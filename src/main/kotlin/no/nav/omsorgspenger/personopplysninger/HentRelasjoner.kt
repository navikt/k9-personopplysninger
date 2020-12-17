package no.nav.omsorgspenger.personopplysninger

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.river.BehovssekvensPacketListener
import no.nav.k9.rapid.river.leggTilLøsning
import no.nav.k9.rapid.river.requireArray
import no.nav.k9.rapid.river.skalLøseBehov
import org.slf4j.LoggerFactory

internal class HentRelasjoner(
        rapidsConnection: RapidsConnection,
        internal val relasjonMediator: RelasjonMediator) : BehovssekvensPacketListener(
        logger = LoggerFactory.getLogger(HentRelasjoner::class.java)
) {

    init {
        River(rapidsConnection).apply {
            validate { packet ->
                packet.skalLøseBehov(BEHOV)
                packet.require(IDENTITETSNUMMER) { it.require() }
                packet.require(TIL){ it.requireArray { entry -> entry is TextNode } }
            }
        }.register(this)
    }

    override fun handlePacket(id: String, packet: JsonMessage): Boolean {
        logger.info("Mottatt $BEHOV").also { incMottattBehov(BEHOV) }

        val identitetsnummer = packet[IDENTITETSNUMMER].asText()

        val til = (packet[TIL] as ArrayNode)
                .map { it.asText() }
                .toSet()

        val løsning = hentRelasjonerFor(
                identitetsnummer = identitetsnummer,
                til = til,
                correlationId = packet[Behovsformat.CorrelationId].asText())

        packet.leggTilLøsning(
                behov = BEHOV,
                løsning = løsning
        )

        return true
    }

    override fun onSent(id: String, packet: JsonMessage) {
        logger.info("Løst behov $BEHOV").also { incLostBehov(BEHOV) }
    }

    private fun hentRelasjonerFor(identitetsnummer: String, til: Set<String>, correlationId: String) = try {
        runBlocking {
            relasjonMediator.hentRelasjoner(
                identitetsnummer = identitetsnummer,
                til = til,
                correlationId = correlationId)
        }
    } catch (cause: Throwable) {
        incBehandlingFeil(BEHOV)
        throw cause
    }

    internal companion object {
        const val BEHOV = "VurderRelasjoner"
        const val IDENTITETSNUMMER = "@behov.$BEHOV.identitetsnummer"
        const val TIL = "@behov.$BEHOV.til"
    }
}
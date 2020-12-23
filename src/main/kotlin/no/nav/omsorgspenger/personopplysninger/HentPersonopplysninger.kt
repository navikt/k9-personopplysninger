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

internal class HentPersonopplysninger(
    rapidsConnection: RapidsConnection,
    internal val personopplysningerMediator: PersonopplysningerMediator
) : BehovssekvensPacketListener(
    logger = LoggerFactory.getLogger(HentPersonopplysninger::class.java)
) {

    init {
        River(rapidsConnection).apply {
            validate { packet ->
                packet.skalLøseBehov(BEHOV)
                packet.require(IDENTITETSNUMMER) { it.requireArray { entry -> entry is TextNode } }
                packet.require(ATTRIBUTER) { it.require() }
                packet.interestedIn(MÅSETTES)
            }
        }.register(this)
    }

    override fun handlePacket(id: String, packet: JsonMessage): Boolean {
        logger.info("Mottatt $BEHOV").also { incMottattBehov(BEHOV) }

        val identitetsnummer = (packet[IDENTITETSNUMMER] as ArrayNode)
            .map { it.asText() }
            .toSet()

        val behovsAttributer: Set<String> = (packet[ATTRIBUTER] as ArrayNode)
            .map { it.asText() }
            .toSet()

        val måSettes = packet[MÅSETTES].asBoolean()

        logger.info("Løser behov før ${identitetsnummer.size} person(er).")

        val løsning = hentPersonopplysningerFor(
            identitetsnummer = identitetsnummer,
            behovsAttributer = behovsAttributer,
            correlationId = packet[Behovsformat.CorrelationId].asText()
        )

        if (måSettes) {
            require(identitetsnummer.size == løsning.values.size) {
                "Uventet feil: måFinneAllePersoner. Forventet opplysninger for ${identitetsnummer.size} identitetsnummer men fann ${løsning.values.size}"
            }
        }

        packet.leggTilLøsning(
            behov = BEHOV,
            løsning = løsning
        )
        return true
    }

    override fun onSent(id: String, packet: JsonMessage) {
        logger.info("Løst behov $BEHOV").also { incLostBehov(BEHOV) }
    }

    private fun hentPersonopplysningerFor(
        identitetsnummer: Set<String>, behovsAttributer: Set<String>, correlationId: String
    ) = try {
        runBlocking {
            personopplysningerMediator.hentPersonopplysninger(
                identitetsnummer = identitetsnummer,
                behovsAttributer = behovsAttributer,
                correlationId = correlationId
            )
        }
    } catch (cause: Throwable) {
        incBehandlingFeil(BEHOV)
        throw cause
    }

    internal companion object {
        const val BEHOV = "HentPersonopplysninger"
        const val IDENTITETSNUMMER = "@behov.$BEHOV.identitetsnummer"
        const val ATTRIBUTER = "@behov.$BEHOV.attributter"
        const val MÅSETTES = "@behov.$BEHOV.måFinneAllePersoner"
    }
}
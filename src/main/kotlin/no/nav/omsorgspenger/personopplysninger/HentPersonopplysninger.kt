package no.nav.omsorgspenger.personopplysninger

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.river.BehovssekvensPacketListener
import no.nav.k9.rapid.river.leggTilLøsning
import no.nav.k9.rapid.river.requireArray
import no.nav.k9.rapid.river.skalLøseBehov
import no.nav.omsorgspenger.personopplysninger.PersonopplysningerMediator.Companion.PersonopplysningerKey
import org.slf4j.LoggerFactory

internal class HentPersonopplysninger(
    rapidsConnection: RapidsConnection,
    internal val personopplysningerMediator: PersonopplysningerMediator
) : BehovssekvensPacketListener(
    logger = LoggerFactory.getLogger(HentPersonopplysninger::class.java)) {

    init {
        River(rapidsConnection).apply {
            validate { packet ->
                packet.skalLøseBehov(BEHOV)
                packet.require(IDENTITETSNUMMER) { it.requireArray { entry -> entry is TextNode } }
                packet.require(ATTRIBUTER) { it.require() }
                packet.interestedIn(MÅ_FINNE_ALLE_PERSONER)
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

        val måFinneAllePersoner = when(packet[MÅ_FINNE_ALLE_PERSONER].isMissingOrNull()) {
            true -> false
            false -> packet[MÅ_FINNE_ALLE_PERSONER].asBoolean(false)
        }

        logger.info("Løser behov før ${identitetsnummer.size} person(er). måFinneAllePersoner=$måFinneAllePersoner")

        val løsning = hentPersonopplysningerFor(
            identitetsnummer = identitetsnummer,
            behovsAttributer = behovsAttributer,
            correlationId = packet[Behovsformat.CorrelationId].asText()
        )

        val funnetPersonopplysningerFor = løsning.getValue(PersonopplysningerKey).keys

        when (måFinneAllePersoner) {
            true -> require(funnetPersonopplysningerFor == identitetsnummer) {
                "Fant ikke personopplysninger for alle etterspurte personer."
            }
            false -> require(identitetsnummer.containsAll(funnetPersonopplysningerFor)) {
                "Fant personopplysninger for ikke-etterspurte personer."
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
        const val MÅ_FINNE_ALLE_PERSONER = "@behov.$BEHOV.måFinneAllePersoner"
    }
}
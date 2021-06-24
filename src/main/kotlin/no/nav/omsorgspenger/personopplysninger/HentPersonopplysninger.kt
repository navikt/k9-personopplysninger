package no.nav.omsorgspenger.personopplysninger

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.river.*
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
                packet.aktueltBehovOrNull()?.also { aktueltBehov ->
                    packet.require(aktueltBehov.IDENTITETSNUMMER()) { it.requireArray { entry -> entry is TextNode } }
                    packet.require(aktueltBehov.ATTRIBUTER()) { it.require() }
                    packet.interestedIn(aktueltBehov.MÅ_FINNE_ALLE_PERSONER())
                }

            }
        }.register(this)
    }

    override fun handlePacket(id: String, packet: JsonMessage): Boolean {
        val aktueltBehov = packet.aktueltBehov()
        logger.info("Mottatt behov $aktueltBehov")

        val identitetsnummer = (packet[aktueltBehov.IDENTITETSNUMMER()] as ArrayNode)
            .map { it.asText() }
            .toSet()

        val behovsAttributer: Set<String> = (packet[aktueltBehov.ATTRIBUTER()] as ArrayNode)
            .map { it.asText() }
            .toSet()

        val måFinneAllePersoner = when(packet[aktueltBehov.MÅ_FINNE_ALLE_PERSONER()].isMissingOrNull()) {
            true -> false
            false -> packet[aktueltBehov.MÅ_FINNE_ALLE_PERSONER()].asBoolean(false)
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
            behov = aktueltBehov,
            løsning = løsning
        )
        return true
    }

    private fun hentPersonopplysningerFor(identitetsnummer: Set<String>, behovsAttributer: Set<String>, correlationId: String) = runBlocking {
        personopplysningerMediator.hentPersonopplysninger(
            identitetsnummer = identitetsnummer,
            behovsAttributer = behovsAttributer,
            correlationId = correlationId
        )
    }

    internal companion object {
        const val BEHOV = "HentPersonopplysninger"
        fun String.IDENTITETSNUMMER() = "@behov.$this.identitetsnummer"
        fun String.ATTRIBUTER() = "@behov.$this.attributter"
        fun String.MÅ_FINNE_ALLE_PERSONER() = "@behov.$this.måFinneAllePersoner"
    }
}
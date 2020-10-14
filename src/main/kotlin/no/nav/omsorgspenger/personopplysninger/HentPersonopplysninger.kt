package no.nav.omsorgspenger.personopplysninger

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.k9.rapid.river.BehovssekvensPacketListener
import no.nav.k9.rapid.river.skalLøseBehov
import org.slf4j.LoggerFactory

internal class HentPersonopplysninger(
        rapidsConnection: RapidsConnection) : BehovssekvensPacketListener(
        logger = LoggerFactory.getLogger(HentPersonopplysninger::class.java)
) {

    init {
        River(rapidsConnection).apply {
            validate { packet ->
                packet.skalLøseBehov(BEHOV)
                packet.require(IDENTITETSNUMMER, JsonNode::asText)
            }
        }.register(this)
    }

    override fun handlePacket(id: String, packet: JsonMessage): Boolean {
        logger.info("Mottatt $BEHOV med id $id")
        return false
    }

    override fun onSent(id: String, packet: JsonMessage) {
        logger.info("Løst behov $BEHOV med id $id")
    }

    internal companion object {
        const val BEHOV = "HentPersonopplysninger"
        const val IDENTITETSNUMMER = "@behov.$BEHOV.identitetsnummer"
    }
}
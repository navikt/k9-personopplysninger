package no.nav.omsorgspenger.personopplysninger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.full.memberProperties
import no.nav.omsorgspenger.personopplysninger.pdl.HentPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class PersonopplysningerMediator(
        internal val pdlClient: PdlClient
) {
    private val secureLogger = LoggerFactory.getLogger("tjenestekall")

    suspend fun hentPersonopplysninger(identitetsnummer: Set<String>, correlationId: String): LøsningsMap {

        val response = pdlClient.getPersonInfo(identitetsnummer, correlationId)
        if (!response.errors.isNullOrEmpty()) {
            secureLogger.error("Fann feil vid hent av data fra PDL: ", response.errors.toString())
        }

        val resultat = mapOf(
                "personopplysninger" to identitetsnummer
                        .map { it to response.toLøsning(it) }
                        .filterNot { it.second.isNullOrEmpty() }
                        .toMap())

        require(!resultat["personopplysninger"].isNullOrEmpty()) { "Lyckades inte parsa data fra PDL" }
        return resultat
    }

    private fun HentPdlResponse.toLøsning(identitetsnummer: String): Map<String, Any> {
        var attributer = mutableMapOf<String, Any>()

        this.data.hentPersonBolk?.filter { it.ident == identitetsnummer }
                ?.map {
                    it.person?.navn?.get(0)?.asMap()?.let { navn -> attributer.put("navn", navn) }
                    it.person?.foedsel?.get(0)?.foedselsdato?.let { fødselsdato -> attributer.put("fødselsdato", fødselsdato) }
                }

        this.data.hentIdenterBolk?.filter { it.ident == identitetsnummer }
                ?.map {
                    it.identer?.get(0)?.ident?.let { ident -> attributer.put("aktørId", ident) }
                }



        return attributer.toMap()

    }

    inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    private companion object {
        val objectMapper: ObjectMapper = jacksonObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(JavaTimeModule())
    }

}

typealias LøsningsMap = Map<String, Map<String, Any>>
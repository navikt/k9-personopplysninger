package no.nav.omsorgspenger.personopplysninger

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

        return identitetsnummer
                .map { it to mutableMapOf(
                        "personopplysninger" to response.toLøsning(it)) }
                .filter { !it.second["personopplysninger"].isNullOrEmpty() }
                .toMap()
    }

    private fun HentPdlResponse.toLøsning(identitetsnummer: String): Map<String, Any> {
        var attributer = mutableMapOf<String, Any>()

        if (!this.data.hentPersonBolk.isNullOrEmpty())
            this.data.hentPersonBolk.filter { it.ident == identitetsnummer }
                    .map {
                        it.person?.navn?.get(0)?.asMap()?.let { navn -> attributer.put("navn", navn) }
                        it.person?.foedsel?.get(0)?.foedselsdato?.let { fødselsdato -> attributer.put("fødseldato", fødselsdato) }
                    }

        if (!this.data.hentIdenterBolk.isNullOrEmpty())
            this.data.hentIdenterBolk.filter { it.ident == identitetsnummer }
                    .map {
                        it.identer?.get(0)?.ident?.let { ident -> attributer.put("aktørId", ident) }
                    }

        return attributer.toMap()

    }

    inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }
}

typealias LøsningsMap = Map<String, Map<String, Any>>
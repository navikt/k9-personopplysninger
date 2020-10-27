package no.nav.omsorgspenger.personopplysninger

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

        return identitetsnummer.map { it to response.toLøsning(it) }.toMap()

    }

    private fun HentPdlResponse.toLøsning(identitetsnummer: String): Map<String, String?> {
        var løsning = mutableMapOf<String, String?>()

        if (!this.data.hentPersonBolk.isNullOrEmpty())
            this.data.hentPersonBolk.filter { it.ident == identitetsnummer }
                    .mapNotNull {
                        løsning.put("navn", it.person?.navn?.get(0).toString() )
                        løsning.put("fødseldato", it.person?.foedsel?.get(0)?.foedselsdato)
                    }

        if (!this.data.hentIdenterBolk.isNullOrEmpty())
            this.data.hentIdenterBolk.filter { it.ident == identitetsnummer }
                    .mapNotNull {
                        løsning.put("aktørId", it.identer?.get(0)?.ident)
                    }

        return løsning

    }
}

typealias LøsningsMap = Map<String, Map<String, String?>>
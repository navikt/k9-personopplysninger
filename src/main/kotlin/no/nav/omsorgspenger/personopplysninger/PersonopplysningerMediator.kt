package no.nav.omsorgspenger.personopplysninger

import kotlin.reflect.full.memberProperties
import no.nav.omsorgspenger.personopplysninger.pdl.HentPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class PersonopplysningerMediator(
        internal val pdlClient: PdlClient
) {
    private val secureLogger = LoggerFactory.getLogger("tjenestekall")

    suspend fun hentPersonopplysninger(identitetsnummer: Set<String>, behovsAttributer: Set<String>, correlationId: String): LøsningsMap {

        val response = pdlClient.getPersonInfo(identitetsnummer, correlationId)
        if (!response.errors.isNullOrEmpty()) {
            secureLogger.error("Fann feil vid hent av data fra PDL: ", response.errors.toString())
        }

        val resultat = mapOf(
                "personopplysninger" to identitetsnummer
                        .map { it to response.toLøsning(it, behovsAttributer) }
                        .filterNot { it.second.isNullOrEmpty() }
                        .toMap())

        require(!resultat["personopplysninger"].isNullOrEmpty()) { "Parsing av data fra PDL gav tomt resultat." }
        return resultat
    }

    private fun HentPdlResponse.toLøsning(identitetsnummer: String, behovsAttributer: Set<String>): Map<String, Any?> {
        var attributer = mutableMapOf<String, Any?>()

        this.data.hentPersonBolk?.filter { it.ident == identitetsnummer && it.code == "ok" }
                ?.map {
                    it.person?.let { person ->
                        person.navn?.firstOrNull()?.let { navn ->
                            navn.asMap().let { attributer.put("navn", it) }
                        }
                        person.foedsel?.firstOrNull()?.let { foedsel ->
                            foedsel.foedselsdato.let { attributer.put("fødselsdato", it) }
                        }
                        person.adressebeskyttelse?.firstOrNull()?.let { adressebeskyttelse ->
                            adressebeskyttelse.gradering.let { attributer.put("adressebeskyttelse", it) }
                        }
                    }
                }

        this.data.hentIdenterBolk?.filter { it.ident == identitetsnummer && it.code == "ok" }
                ?.map {
                    it.identer?.firstOrNull()?.let {
                        it.ident.let { attributer.put("aktørId", it) }
                    }
                }

        return attributer.toMap().filterKeys { behov ->
            behovsAttributer.contains(behov)
        }

    }

    inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

}

typealias LøsningsMap = Map<String, Map<String, Any>>
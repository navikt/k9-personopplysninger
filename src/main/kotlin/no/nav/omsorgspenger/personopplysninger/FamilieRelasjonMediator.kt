package no.nav.omsorgspenger.personopplysninger

import kotlin.reflect.full.memberProperties
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class FamilieRelasjonMediator(
    internal val pdlClient: PdlClient
) {
    private val secureLogger = LoggerFactory.getLogger("tjenestekall")

    suspend fun hentFamilieRelasjon(identitetsnummer: String, correlationId: String): RelasjonsLøsningsMap {
        val response = pdlClient.hentFamilierelasjoner(identitetsnummer, correlationId)

        if (!response.errors.isNullOrEmpty()) {
            secureLogger.error("Fann feil vid hent av data fra PDL: ", response.errors.toString())
            throw IllegalStateException("Fann feil vid hent av data fra PDL")
        }

        var losning = mutableListOf<Map<String, Any>>()

        response.data.hentPersonBolk.first()
            .person?.familierelasjoner?.forEach { relatertPerson ->
                var resultat = mutableMapOf<String, Any>()
                when {
                    relatertPerson.minRolleForPerson == HentRelasjonPdlResponse.Relasjon.BARN -> {
                        resultat["relasjon"] = relatertPerson.minRolleForPerson.toString()
                    }
                    relatertPerson.relatertPersonsRolle.erForelder() -> {
                        resultat["relasjon"] = "BARN"
                    }
                    else -> {
                        resultat["relasjon"] = "INGEN"
                    }
                }
                resultat["identitetsnummer"] = relatertPerson.relatertPersonsIdent
                resultat["borSammen"] = false

                losning.add(resultat.toMap())
            }

        return mapOf(FamilieRelasjonerKey to losning)

    }

    private inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    private companion object {
        private const val FamilieRelasjonerKey = "relasjoner"
    }

    private fun HentRelasjonPdlResponse.Relasjon.erForelder() : Boolean {
        return (this == HentRelasjonPdlResponse.Relasjon.FAR || this == HentRelasjonPdlResponse.Relasjon.MOR)
    }

}

typealias RelasjonsLøsningsMap = Map<String, List<Map<String, Any>>>
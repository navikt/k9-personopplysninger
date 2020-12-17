package no.nav.omsorgspenger.personopplysninger

import kotlin.reflect.full.memberProperties
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse.Relasjon
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class RelasjonMediator(
        internal val pdlClient: PdlClient
) {
    private val secureLogger = LoggerFactory.getLogger("tjenestekall")

    suspend fun hentRelasjoner(identitetsnummer: String, til: Set<String>, correlationId: String): RelasjonsLøsningsMap {
        val response = pdlClient.HentRelasjonInfo(til.plus(identitetsnummer), correlationId)

        if (!response.errors.isNullOrEmpty()) {
            secureLogger.error("Fann feil vid hent av data fra PDL: ", response.errors.toString())
            throw IllegalStateException("Fann feil vid hent av data fra PDL")
        }

        var relasjonsListe = mutableListOf<Map<String, Any>>()

        val søkersMatrikkelId = response.data.hentPersonBolk
                .filter { it.ident == identitetsnummer && it.code == "ok" }
                .first().person?.bostedsadresse?.first()?.matrikkeladresse?.matrikkelId.orEmpty()

        response.data.hentPersonBolk
                .filter { it.code == "ok" && it.ident != identitetsnummer && it.person != null }
                .map { personBolk ->
                    personBolk.håndterResponse(identitetsnummer, søkersMatrikkelId).let {
                        if (!it.isNullOrEmpty()) relasjonsListe.add(it)
                    }
                }

        return mapOf(RelasjonerKey to relasjonsListe)

    }

    private fun HentRelasjonPdlResponse.PersonBolk.håndterResponse(søkersIdentitetsnummer: String, søkersMatrikkelId: String): Map<String, Any> {
        var resultat = mutableMapOf<String, Any>()

        this.person!!.familierelasjoner
                .filter { it.relatertPersonsIdent != søkersIdentitetsnummer }
                .forEach { relatertPerson ->
                    when {
                        relatertPerson.relatertPersonsRolle!!.erBarn() -> resultat["relasjon"] = relatertPerson.minRolleForPerson.toString()
                        relatertPerson.relatertPersonsRolle.erForelder() -> resultat["relasjon"] = relatertPerson.minRolleForPerson.toString()
                        else -> resultat["relasjon"] = "INGEN"
                    }
                }

        resultat["identitetsnummer"] = this.ident

        val harDeltBosted = !this.person.deltBosted?.firstOrNull()?.matrikkeladresse?.matrikkelId.isNullOrEmpty()
        val harBostadsAdresse = !this.person.bostedsadresse?.firstOrNull()?.matrikkeladresse?.matrikkelId.isNullOrEmpty()
        when {
            harDeltBosted -> resultat["borSammen"] = this.person.deltBosted?.firstOrNull()?.matrikkeladresse?.matrikkelId.toString() == søkersMatrikkelId
            harBostadsAdresse -> resultat["borSammen"] = this.person.bostedsadresse?.firstOrNull()?.matrikkeladresse?.matrikkelId.toString() == søkersMatrikkelId
            else -> resultat["borSammen"] = false
        }


        return resultat.toMap()
    }

    private companion object {
        private const val RelasjonerKey = "relasjoner"
    }

    private fun Relasjon.erForelder(): Boolean {
        return (this == Relasjon.FAR || this == Relasjon.MOR)
    }

    private fun Relasjon.erBarn(): Boolean {
        return (this == Relasjon.BARN)
    }

}

typealias RelasjonsLøsningsMap = Map<String, List<Map<String, Any>>>
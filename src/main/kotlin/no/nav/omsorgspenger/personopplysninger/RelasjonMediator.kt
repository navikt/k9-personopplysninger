package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.adresser
import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.inneholderMinstEn
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse.Relasjon
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class RelasjonMediator(
    internal val pdlClient: PdlClient) {
    private val secureLogger = LoggerFactory.getLogger("tjenestekall")

    suspend fun hentRelasjoner(
        identitetsnummer: String,
        til: Set<String>,
        correlationId: String
    ): RelasjonsLøsningsMap {
        val response = pdlClient.HentRelasjonInfo(til.plus(identitetsnummer), correlationId)

        if (!response.errors.isNullOrEmpty()) {
            secureLogger.error("Fann feil vid hent av data fra PDL: ", response.errors.toString())
            throw IllegalStateException("Fann feil vid hent av data fra PDL")
        }

        val relasjonsListe = mutableListOf<Map<String, Any>>()

        val søkersAdresser = response.data.hentPersonBolk.first {
            it.ident == identitetsnummer && it.code == "ok" && it.person != null
        }.person!!.adresser()

        response.data.hentPersonBolk
            .filter { it.code == "ok" && it.ident != identitetsnummer && it.person != null }
            .map { personBolk ->
                relasjonsListe.add(
                    personBolk.håndterResponse(identitetsnummer, søkersAdresser)
                )
            }

        response.data.hentPersonBolk
            .filter { it.person == null || it.code != "ok" }
            .forEach {
                relasjonsListe.add(
                    mapOf("relasjon" to "INGEN",
                        "borSammen" to false,
                        "identitetsnummer" to it.ident
                    )
                )
            }

        require(relasjonsListe.size == til.size) { "Uventet feil: forventede ${til.size} relasjoner i svar, fick ${relasjonsListe.size}!" }
        return mapOf(RelasjonerKey to relasjonsListe)

    }

    private fun HentRelasjonPdlResponse.PersonBolk.håndterResponse(
        søkersIdentitetsnummer: String,
        søkersAdresser: List<Adresse>
    ): Map<String, Any> {
        val resultat = mutableMapOf<String, Any>()

        if (this.person!!.familierelasjoner.isNotEmpty()) {
            this.person.familierelasjoner
                .forEach { relatertPerson ->
                    if (relatertPerson.erSøkersBarn(søkersIdentitetsnummer)) {
                        resultat["relasjon"] = "BARN"
                    }
                }
        }

        if (resultat["relasjon"] == null) {
            resultat["relasjon"] = "INGEN"
        }
        resultat["identitetsnummer"] = this.ident
        resultat["borSammen"] = søkersAdresser.inneholderMinstEn(this.person.adresser())

        return resultat.toMap()
    }

    private companion object {
        private const val RelasjonerKey = "relasjoner"
    }

    private fun HentRelasjonPdlResponse.FamilieRelasjoner.erSøkersBarn(identitetsnummer: String): Boolean {
        return minRolleForPerson == Relasjon.BARN && relatertPersonsIdent == identitetsnummer
    }
}

typealias RelasjonsLøsningsMap = Map<String, List<Map<String, Any>>>
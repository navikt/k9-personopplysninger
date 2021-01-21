package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse.Relasjon
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse.Person
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class RelasjonMediator(
    internal val pdlClient: PdlClient
) {
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

        var relasjonsListe = mutableListOf<Map<String, Any>>()

        val søkersAdresse = response.data.hentPersonBolk
            .filter { it.ident == identitetsnummer && it.code == "ok" && it.person != null }
            .first().person!!.hentAdresse()

        response.data.hentPersonBolk
            .filter { it.code == "ok" && it.ident != identitetsnummer && it.person != null }
            .map { personBolk ->
                relasjonsListe.add(
                    personBolk.håndterResponse(identitetsnummer, søkersAdresse)
                )
            }

        response.data.hentPersonBolk
            .filter { it.person == null || it.code != "ok" }
            .forEach {
                relasjonsListe.add(
                    mapOf("relasjon" to "INGEN",
                        "borSammen" to false,
                        "identitetsnummer" to it.ident)
                )
            }

        require(relasjonsListe.size == til.size) { "Uventet feil: forventede ${til.size} relasjoner i svar, fick ${relasjonsListe.size}!" }
        return mapOf(RelasjonerKey to relasjonsListe)

    }

    private fun HentRelasjonPdlResponse.PersonBolk.håndterResponse(
        søkersIdentitetsnummer: String,
        søkersAdresse: Adresse
    ): Map<String, Any> {
        var resultat = mutableMapOf<String, Any>()

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
        resultat["borSammen"] = this.person.hentAdresse() == søkersAdresse

        return resultat.toMap()
    }

    private companion object {
        private const val RelasjonerKey = "relasjoner"
    }

    private fun HentRelasjonPdlResponse.FamilieRelasjoner.erSøkersBarn(identitetsnummer: String): Boolean {
        return minRolleForPerson == Relasjon.BARN && relatertPersonsIdent == identitetsnummer
    }

    private fun Person.hentAdresse(): Adresse {
        return Adresse(
            bostedMatrikkelId = bostedsadresse.firstOrNull { it?.vegadresse?.matrikkelId != null }?.vegadresse?.matrikkelId,
            bostedVegadresse = bostedsadresse.firstOrNull { it.vegadresse?.adressenavn != null }?.vegadresse?.adressenavn,
            deltBostedMatrikkelId = deltBosted.firstOrNull { it.vegadresse?.matrikkelId != null }?.vegadresse?.matrikkelId,
            deltBostedVegadresse = deltBosted.firstOrNull { it.vegadresse?.adressenavn != null }?.vegadresse?.adressenavn
        )
    }

    internal data class Adresse(
        private val bostedMatrikkelId: String?,
        private val bostedVegadresse: String?,
        private val deltBostedMatrikkelId: String?,
        private val deltBostedVegadresse: String?
    ) {
        override fun equals(other: Any?) : Boolean {
            if(other !is Adresse) return false
            val sammeVegAdresse = when {
                erLike(bostedVegadresse, other.bostedVegadresse) -> true
                erLike(bostedVegadresse, other.deltBostedVegadresse) -> true
                erLike(deltBostedVegadresse, other.deltBostedVegadresse) -> true
                erLike(deltBostedVegadresse, other.bostedVegadresse) -> true
                else -> false
            }
            val sammeMatrikkelId = when {
                erLike(bostedMatrikkelId, other.bostedMatrikkelId) -> true
                erLike(bostedMatrikkelId, other.deltBostedMatrikkelId) -> true
                erLike(deltBostedMatrikkelId, other.bostedMatrikkelId) -> true
                erLike(deltBostedMatrikkelId, other.deltBostedMatrikkelId) -> true
                else -> false
            }

            if(sammeVegAdresse) {
                return if(bådeHarMatrikkelId(other)) {
                    sammeMatrikkelId
                } else {
                    sammeVegAdresse
                }
            }

            return sammeMatrikkelId
        }

        private fun erLike(a: String?, b: String?): Boolean {
            val beggeSatt = (a != null && a.isNotBlank()) && (b != null && b.isNotBlank())
            return beggeSatt && (a == b)
        }

        private fun bådeHarMatrikkelId(other: Any?): Boolean {
            if(other !is Adresse) return false
            if(!bostedMatrikkelId.isNullOrEmpty() && !other.bostedMatrikkelId.isNullOrEmpty()) return true
            if(!deltBostedMatrikkelId.isNullOrEmpty() && !other.deltBostedMatrikkelId.isNullOrEmpty()) return true
            if(!bostedMatrikkelId.isNullOrEmpty() && !other.deltBostedMatrikkelId.isNullOrEmpty()) return true
            if(!deltBostedMatrikkelId.isNullOrEmpty() && !other.bostedMatrikkelId.isNullOrEmpty()) return true
            return false
        }

    }

}

typealias RelasjonsLøsningsMap = Map<String, List<Map<String, Any>>>
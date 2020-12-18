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

    suspend fun hentRelasjoner(identitetsnummer: String, til: Set<String>, correlationId: String): RelasjonsLøsningsMap {
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
                    personBolk.håndterResponse(identitetsnummer, søkersAdresse).let {
                        relasjonsListe.add(it)
                    }
                }

        return mapOf(RelasjonerKey to relasjonsListe)

    }

    private fun HentRelasjonPdlResponse.PersonBolk.håndterResponse(søkersIdentitetsnummer: String, søkersAdresse: Adresse): Map<String, Any> {
        var resultat = mutableMapOf<String, Any>()

        if (!this.person!!.familierelasjoner.isNullOrEmpty()) {
            this.person.familierelasjoner
                    ?.filter { it.relatertPersonsIdent != søkersIdentitetsnummer }
                    ?.forEach { relatertPerson ->
                        relatertPerson.relatertPersonsRolle?.let {
                            if (it.erBarn()) resultat["relasjon"] = relatertPerson.minRolleForPerson.toString()
                        }
                        relatertPerson.relatertPersonsRolle?.let {
                            if (it.erForelder()) resultat["relasjon"] = relatertPerson.minRolleForPerson.toString()
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

    private fun Relasjon.erForelder(): Boolean = (this == Relasjon.FAR || this == Relasjon.MOR)
    private fun Relasjon.erBarn(): Boolean = (this == Relasjon.BARN)

    private fun Person.hentAdresse(): Adresse {
        return Adresse(
                bostedMatrikkelId = bostedsadresse.firstOrNull { it.matrikkeladresse?.matrikkelId != null }?.matrikkeladresse?.matrikkelId,
                bostedVegadresse = bostedsadresse.firstOrNull { it.vegadresse?.adressenavn != null }?.vegadresse?.adressenavn,
                deltBostedMatrikkelId = deltBosted.firstOrNull { it.matrikkeladresse?.matrikkelId != null }?.matrikkeladresse?.matrikkelId,
                deltBostedVegadresse = deltBosted.firstOrNull { it.vegadresse?.adressenavn != null }?.vegadresse?.adressenavn
        )
    }

    internal data class Adresse(
            private val bostedMatrikkelId: String?,
            private val bostedVegadresse: String?,
            private val deltBostedMatrikkelId: String?,
            private val deltBostedVegadresse: String?) {
        override fun equals(other: Any?) = when {
            other !is Adresse -> false
            erLike(bostedMatrikkelId, other.bostedMatrikkelId) -> true
            erLike(bostedMatrikkelId, other.deltBostedMatrikkelId) -> true
            erLike(deltBostedMatrikkelId, other.bostedMatrikkelId) -> true
            erLike(deltBostedMatrikkelId, other.deltBostedMatrikkelId) -> true
            erLike(bostedVegadresse, other.bostedVegadresse) -> true
            erLike(bostedVegadresse, other.deltBostedVegadresse) -> true
            erLike(deltBostedVegadresse, other.deltBostedVegadresse) -> true
            erLike(deltBostedVegadresse, other.bostedVegadresse) -> true
            else -> false
        }

        private fun erLike(a: String?, b: String?): Boolean {
            val beggeSatt = (a != null && a.isNotBlank()) && (b != null && b.isNotBlank())
            return beggeSatt && (a == b)
        }
    }

}

typealias RelasjonsLøsningsMap = Map<String, List<Map<String, Any>>>
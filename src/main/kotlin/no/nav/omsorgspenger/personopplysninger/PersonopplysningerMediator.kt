package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Enhet.Companion.adressebeskyttelseTilEnhet
import no.nav.omsorgspenger.personopplysninger.Enhet.Companion.fellesEnhet
import no.nav.omsorgspenger.personopplysninger.pdl.AdressebeskyttelseGradering.Companion.fellesAdressebeskyttelse
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
            throw IllegalStateException("Fann feil vid hent av data fra PDL")
        }

        val personopplysninger = identitetsnummer
            .map { it to response.toLøsning(it, behovsAttributer) }
            .filter { it.second.keys.containsAll(behovsAttributer) }
            .toMap()

        val resultat = mutableMapOf<String, Map<String, Any>>(
            PersonopplysningerKey to personopplysninger
        )

        if (behovsAttributer.skalLeggeTilFellesopplysnigner()) {
            val fellesopplysninger = mutableMapOf<String, Any>()

            if (behovsAttributer.skalLeggeTilFellesEnhetstype() || behovsAttributer.skalLeggeTilFellesEnhetsnummer()) {
                val fellesEnhet = personopplysninger.map { it.value[EnhetsnummerAttributt].toString() }.fellesEnhet()

                if (behovsAttributer.skalLeggeTilFellesEnhetsnummer()) {
                    fellesopplysninger[EnhetsnummerAttributt] = fellesEnhet.nummer
                }
                if (behovsAttributer.skalLeggeTilFellesEnhetstype()) {
                    fellesopplysninger[EnhetstypeAttributt] = fellesEnhet.type
                }
            }

            if (behovsAttributer.skalLeggeTilFellesAdressbeskyttelse()) {
                fellesopplysninger[AdressebeskyttelseAttributt] = personopplysninger.map {it.value[AdressebeskyttelseAttributt].toString() }.fellesAdressebeskyttelse()
            }
            resultat[FellesopplysningerKey] = fellesopplysninger
        }

        return resultat
    }

    private fun HentPdlResponse.toLøsning(identitetsnummer: String, behovsAttributer: Set<String>): Map<String, Any?> {
        val attributer = mutableMapOf<String, Any?>()

        this.data.hentPersonBolk?.filter { it.ident == identitetsnummer && it.code == "ok" }
                ?.map {
                    it.person?.let { person ->
                        person.navn.firstOrNull()?.let { navn ->
                            val sammensatt = when (navn.mellomnavn) {
                                null -> "${navn.fornavn} ${navn.etternavn}"
                                else -> "${navn.fornavn} ${navn.mellomnavn} ${navn.etternavn}"
                            }
                            navn.asMap().plus("sammensatt" to sammensatt).let { attributer.put("navn", it) }
                        }
                        person.foedsel.firstOrNull()?.let { foedsel ->
                            foedsel.foedselsdato.let { attributer.put("fødselsdato", it) }
                        }
                        attributer[AdressebeskyttelseAttributt] = person.gradering.name
                        val enhet = person.gradering.name.adressebeskyttelseTilEnhet()
                        attributer[EnhetsnummerAttributt] = enhet.nummer
                        attributer[EnhetstypeAttributt] = enhet.type
                    }
                }

        this.data.hentIdenterBolk?.filter { it.ident == identitetsnummer && it.code == "ok" }
                ?.map {
                    it.identer?.map { ident ->
                        if(ident.gruppe == "AKTORID") {
                            attributer["aktørId"] = ident.ident
                        }
                        if(ident.gruppe == "FOLKEREGISTERIDENT") {
                            attributer["gjeldendeIdentitetsnummer"] = ident.ident
                        }
                    }
                }
        return attributer.toMap().filterKeys { behov ->
            behovsAttributer.contains(behov)
        }

    }

    private inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    private fun Set<String>.skalLeggeTilFellesEnhetsnummer() = contains(EnhetsnummerAttributt)
    private fun Set<String>.skalLeggeTilFellesEnhetstype() = contains(EnhetstypeAttributt)
    private fun Set<String>.skalLeggeTilFellesAdressbeskyttelse() = contains(AdressebeskyttelseAttributt)

    private fun Set<String>.skalLeggeTilFellesopplysnigner() =
        skalLeggeTilFellesEnhetsnummer() || skalLeggeTilFellesEnhetstype() || skalLeggeTilFellesAdressbeskyttelse()

    internal companion object {
        internal const val PersonopplysningerKey = "personopplysninger"
        private const val FellesopplysningerKey = "fellesopplysninger"

        private const val EnhetsnummerAttributt = "enhetsnummer"
        private const val EnhetstypeAttributt = "enhetstype"

        private const val AdressebeskyttelseAttributt = "adressebeskyttelse"

    }

}

typealias LøsningsMap = Map<String, Map<String, Any>>
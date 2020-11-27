package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Enhetsnummer.adressebeskyttelseTilEnhetnummer
import no.nav.omsorgspenger.personopplysninger.Enhetsnummer.fellesEnhetsnummer
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

        val resultat = mapOf(
            "personopplysninger" to personopplysninger
        ).let { it ->
            when (behovsAttributer.contains(EnhetsnummerAttributt)) {
                true -> it.plus("fellesopplysninger" to mapOf(
                    EnhetsnummerAttributt to personopplysninger.map { it.value[EnhetsnummerAttributt].toString() }.fellesEnhetsnummer()
                ))
                false -> it
            }
        }

        return resultat
    }

    private fun HentPdlResponse.toLøsning(identitetsnummer: String, behovsAttributer: Set<String>): Map<String, Any?> {
        val attributer = mutableMapOf<String, Any?>()

        this.data.hentPersonBolk?.filter { it.ident == identitetsnummer && it.code == "ok" }
                ?.map {
                    it.person?.let { person ->
                        person.navn.firstOrNull()?.let { navn ->
                            navn.asMap().let { attributer.put("navn", it) }
                        }
                        person.foedsel.firstOrNull()?.let { foedsel ->
                            foedsel.foedselsdato.let { attributer.put("fødselsdato", it) }
                        }
                        attributer["adressebeskyttelse"] = person.gradering.name
                        attributer[EnhetsnummerAttributt] = person.gradering.name.adressebeskyttelseTilEnhetnummer()
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

    private companion object {
        private const val EnhetsnummerAttributt = "enhetsnummer"
    }

}

typealias LøsningsMap = Map<String, Map<String, Any>>
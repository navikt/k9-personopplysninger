package no.nav.klage.clients.pdl

import kotlin.reflect.full.memberProperties

data class HentPdlResponse(val data: HentPersonInfo, val errors: List<PdlError>?)

data class HentPersonInfo(val hentPerson: Person?, val hentIdenter: Identer?)

data class Person(
        val navn: List<Navn>,
        val adressebeskyttelse: List<Adressebeskyttelse>,
        val foedsel: List<Foedsel>
)

data class Adressebeskyttelse(
        val gradering: AdressebeskyttelseGradering
)

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT,
}

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

data class Foedsel(
        val foedselsdato: String
)

data class Identer(
        val identer: List<Ident>
)

data class Ident(
        val ident: String
)
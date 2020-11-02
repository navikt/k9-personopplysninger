package no.nav.omsorgspenger.personopplysninger.pdl

data class HentPdlResponse(val data: HentPersonBolkInfo, val errors: List<PdlError>?)
data class HentPersonBolkInfo(val hentPersonBolk: List<PersonBolk>?, val hentIdenterBolk: List<IdenterBolk>?)

data class PersonBolk(
        val ident: String,
        val person: Person?,
        val code: String
)

data class IdenterBolk(
        val ident: String,
        val identer: List<Ident>?,
        val code: String
)

data class Ident(
        val ident: String
)

data class Person(
        val navn: List<Navn>,
        val adressebeskyttelse: List<Adressebeskyttelse>?,
        val foedsel: List<Foedsel>
)

data class Adressebeskyttelse(
        val gradering: AdressebeskyttelseGradering?
)

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

data class Foedsel(
        val foedselsdato: String?
)

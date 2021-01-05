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
        val ident: String,
        val gruppe: String
)

data class Person(
        val navn: List<Navn>,
        val adressebeskyttelse: List<Adressebeskyttelse>,
        val foedsel: List<Foedsel>) {
    internal val gradering = when {
        adressebeskyttelse.isEmpty() ->
            AdressebeskyttelseGradering.UGRADERT
        adressebeskyttelse.size > 1 ->
            throw IllegalStateException("Forventet ikke ${adressebeskyttelse.size} adressebeskyttelser uten historikk.")
        else ->
            adressebeskyttelse.first().gradering ?: AdressebeskyttelseGradering.UGRADERT

    }
}

data class Adressebeskyttelse(
        val gradering: AdressebeskyttelseGradering?
)

enum class AdressebeskyttelseGradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT;

    internal companion object {
        internal fun Collection<String>.fellesAdressebeskyttelse(): AdressebeskyttelseGradering {
            val adressebeskyttelse = map { valueOf(it) }
            return when {
                adressebeskyttelse.any { it == STRENGT_FORTROLIG_UTLAND } -> STRENGT_FORTROLIG_UTLAND
                adressebeskyttelse.any { it == STRENGT_FORTROLIG } -> STRENGT_FORTROLIG
                adressebeskyttelse.any { it == FORTROLIG } -> FORTROLIG
                else -> UGRADERT
            }
        }
    }
}

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

data class Foedsel(
        val foedselsdato: String?
)

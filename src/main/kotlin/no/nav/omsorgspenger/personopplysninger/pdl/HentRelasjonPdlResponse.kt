package no.nav.omsorgspenger.personopplysninger.pdl

data class HentRelasjonPdlResponse(val data: HentPersonBolkInfo, val errors: List<PdlError>?) {
    data class HentPersonBolkInfo(val hentPersonBolk: List<PersonBolk>)

    data class PersonBolk(
        val ident: String,
        val person: Person?,
        val code: String
    )

    data class Person(
        val familierelasjoner: List<FamilieRelasjoner>
    )

    data class FamilieRelasjoner(
        val relatertPersonsIdent: String,
        val relatertPersonsRolle: Relasjon,
        val minRolleForPerson: Relasjon?
    )

    enum class Relasjon {
        BARN,
        FAR,
        MOR,
        ANNEN_MOR;
    }
}
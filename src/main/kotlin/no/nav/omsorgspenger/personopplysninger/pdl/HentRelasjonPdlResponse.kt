package no.nav.omsorgspenger.personopplysninger.pdl

data class HentRelasjonPdlResponse(val data: HentPersonBolkInfo, val errors: List<PdlError>?) {
    data class HentPersonBolkInfo(val hentPersonBolk: List<PersonBolk>)

    data class PersonBolk(
            val ident: String,
            val person: Person?,
            val code: String
    )

    data class Person(
            val familierelasjoner: List<FamilieRelasjoner> = emptyList(),
            val bostedsadresse: List<Adresse> = emptyList(),
            val deltBosted: List<Adresse> = emptyList(),
    )

    data class FamilieRelasjoner(
            val relatertPersonsIdent: String?,
            val relatertPersonsRolle: Relasjon?,
            val minRolleForPerson: Relasjon
    )

    data class Adresse(
            val matrikkeladresse: MatrikkelId?,
            val vegadresse: AdresseNavn?
    )

    data class MatrikkelId(
            val matrikkelId: String
    )

    data class AdresseNavn(
            val adressenavn: String
    )

    enum class Relasjon {
        BARN,
        FAR,
        MOR,
        ANNEN_MOR;
    }

}
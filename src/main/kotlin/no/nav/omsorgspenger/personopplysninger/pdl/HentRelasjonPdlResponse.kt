package no.nav.omsorgspenger.personopplysninger.pdl

data class HentRelasjonPdlResponse(
    val data: HentPersonBolkInfo,
    val errors: List<PdlError>?) {

    data class HentPersonBolkInfo(
        val hentPersonBolk: List<PersonBolk>
    )

    data class PersonBolk(
        val ident: String,
        val person: Person?,
        val code: String
    )

    data class Person(
        val familierelasjoner: List<FamilieRelasjoner> = emptyList(),
        val bostedsadresse: List<VegAdresse> = emptyList(),
        val deltBosted: List<VegAdresse> = emptyList()
    )

    data class FamilieRelasjoner(
        val relatertPersonsIdent: String?,
        val relatertPersonsRolle: Relasjon?,
        val minRolleForPerson: Relasjon
    )

    data class VegAdresse(
        val vegadresse: AdresseNavn?
    )

    data class AdresseNavn(
        val matrikkelId: String?,
        val adressenavn: String?
    )

    enum class Relasjon {
        BARN,
        FAR,
        MOR,
        ANNEN_MOR;
    }
}
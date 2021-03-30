package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse

internal data class Adresse(
    internal val matrikkelId: String?,
    internal val adressenavn: String?) {

    init { require(matrikkelId != null || adressenavn != null) {
        "Adressen må inneholde enten matrikkelId eller adressenavn"
    }}

    override fun equals(other: Any?) = when {
        other !is Adresse -> false
        matrikkelId != null && other.matrikkelId != null -> matrikkelId == other.matrikkelId
        adressenavn != null && other.adressenavn != null -> adressenavn == other.adressenavn
        else -> false
    }

    override fun hashCode() = when {
        matrikkelId != null -> matrikkelId.hashCode()
        else -> adressenavn.hashCode()
    }

    internal companion object {
        private val Digits = "\\d".toRegex()

        internal fun List<Adresse>.inneholderMinstEn(andreAdresser: List<Adresse>) =
            intersect(andreAdresser).isNotEmpty()

        internal fun HentRelasjonPdlResponse.Person.adresser() : List<Adresse> {
            val vegadresser = listOfNotNull(
                bostedsadresse.firstOrNull()?.vegadresse?.somAdresse(),
                deltBosted.firstOrNull()?.vegadresse?.somAdresse(),
                oppholdsadresse.firstOrNull()?.vegadresse?.somAdresse(),
                kontaktadresse.firstOrNull()?.vegadresse?.somAdresse()
            )
            val postadresser = kontaktadresse.firstOrNull()?.postAdresseIFrittFormat?.somAdresser() ?: emptyList()
            return vegadresser.plus(postadresser)
        }

        internal fun HentRelasjonPdlResponse.VegAdresse.somAdresse() = when {
            matrikkelId.isNullOrBlank() && adressenavn.isNullOrBlank()-> null
            else -> Adresse(
                matrikkelId = matrikkelId,
                adressenavn = adressenavn?.trimToUppercase()
            )
        }

        private fun String.trimToUppercase() = trim().toUpperCase()
        private fun String.adresselinjeSomAdresse() : Adresse? {
            val førTall = split(Digits)[0]
            return when (førTall.isBlank()) {
                true -> null
                false -> Adresse(matrikkelId = null, adressenavn = førTall.trimToUppercase())
            }
        }

        private fun HentRelasjonPdlResponse.PostAdresseIFrittFormat.somAdresser() = listOfNotNull(
            adresselinje1?.adresselinjeSomAdresse(),
            adresselinje2?.adresselinjeSomAdresse(),
            adresselinje3?.adresselinjeSomAdresse()
        )
    }
}
package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse

internal data class Adresse(
    internal val matrikkelId: String?,
    internal val adressenavn: String?) {

    init { require(!matrikkelId.isNullOrBlank() || !adressenavn.isNullOrBlank()) {
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
        private fun String.blankToNull() = when (isBlank()) {
            true -> null
            false -> this
        }
        private fun String.trimAdressenavn() = blankToNull()?.trim()?.uppercase()
        
        internal fun List<Adresse>.inneholderMinstEn(andreAdresser: List<Adresse>) =
            intersect(andreAdresser).isNotEmpty()

        internal fun HentRelasjonPdlResponse.Person.adresser() : List<Adresse> {
            val vegadresser = listOfNotNull(
                bostedsadresse.firstOrNull()?.vegadresse?.somAdresse(),
                deltBosted.firstOrNull()?.vegadresse?.somAdresse(),
                oppholdsadresse.firstOrNull()?.vegadresse?.somAdresse(),
                kontaktadresse.firstOrNull()?.vegadresse?.somAdresse()
            )
            val postadresser = kontaktadresse.firstOrNull()?.postadresseIFrittFormat?.somAdresser() ?: emptyList()
            return vegadresser.plus(postadresser)
        }

        internal fun HentRelasjonPdlResponse.VegAdresse.somAdresse() = when {
            matrikkelId.isNullOrBlank() && adressenavn.isNullOrBlank() -> null
            else -> Adresse(
                matrikkelId = matrikkelId?.blankToNull(),
                adressenavn = adressenavn?.trimAdressenavn()
            )
        }

        private fun String.adresselinjeSomAdresse() : Adresse? {
            val førTall = split(Digits)[0]
            return when (førTall.isBlank()) {
                true -> null
                false -> Adresse(matrikkelId = null, adressenavn = førTall.trimAdressenavn())
            }
        }

        private fun HentRelasjonPdlResponse.PostAdresseIFrittFormat.somAdresser() = listOfNotNull(
            adresselinje1?.adresselinjeSomAdresse(),
            adresselinje2?.adresselinjeSomAdresse(),
            adresselinje3?.adresselinjeSomAdresse()
        )
    }
}
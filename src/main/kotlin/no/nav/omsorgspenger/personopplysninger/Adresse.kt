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

    override fun hashCode(): Int {
        var result = matrikkelId?.hashCode() ?: 0
        result = 31 * result + (adressenavn?.hashCode() ?: 0)
        return result
    }

    internal companion object {
        internal fun List<Adresse>.inneholderMinstEn(andreAdresser: List<Adresse>) =
            intersect(andreAdresser).isNotEmpty()

        internal fun HentRelasjonPdlResponse.VegAdresse.somAdresse() = when {
            vegadresse == null -> null
            vegadresse.matrikkelId == null && vegadresse.adressenavn == null -> null
            else -> Adresse(
                matrikkelId = vegadresse.matrikkelId,
                adressenavn = vegadresse.adressenavn?.trim()?.toUpperCase()
            )
        }
    }
}
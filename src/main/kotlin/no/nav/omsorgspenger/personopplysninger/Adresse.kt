package no.nav.omsorgspenger.personopplysninger

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

    internal companion object {
        internal fun List<Adresse>.inneholderMinstEn(andreAdresser: List<Adresse>) =
            intersect(andreAdresser).isNotEmpty()
    }
}
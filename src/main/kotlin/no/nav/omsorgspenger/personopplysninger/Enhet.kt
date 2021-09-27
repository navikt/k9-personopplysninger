package no.nav.omsorgspenger.personopplysninger

internal enum class Enhet(internal val nummer: String, internal val type: String) {
    SykdomIFamilien("4487", "VANLIG"),
    Vikafossen("2103", "SKJERMET");

    internal companion object {
        private val tilSykdomIFamilien = setOf("UGRADERT", "FORTROLIG")

        private fun String.fraEnhetsnummer() = values().firstOrNull {
            it.nummer == this
        } ?: throw IllegalArgumentException("Finner ikke enhet med enhetsnummer $this")

        internal fun String.adressebeskyttelseTilEnhet() = when {
            this.uppercase() in tilSykdomIFamilien -> SykdomIFamilien
            else -> Vikafossen
        }

        internal fun Collection<String>.fellesEnhet() = when (map { it.fraEnhetsnummer() }.any { it == Vikafossen }) {
            true -> Vikafossen
            false -> SykdomIFamilien
        }
    }
}


package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Enhetsnummer.Enhet.Companion.fraEnhetsnummer

internal object Enhetsnummer {
    private val tilSykdomIFamilien = setOf("UGRADERT", "FORTROLIG")

    internal fun String.adressebeskyttelseTilEnhetnummer() = when {
        this.toUpperCase() in tilSykdomIFamilien -> Enhet.SykdomIFamilien
        else -> Enhet.Vikafossen
    }.nummer

    internal fun Collection<String>.fellesEnhetsnummer() = when (map { it.fraEnhetsnummer() }.any { it == Enhet.Vikafossen }) {
        true -> Enhet.Vikafossen.nummer
        false -> Enhet.SykdomIFamilien.nummer
    }

    internal enum class Enhet(internal val nummer: String) {
        SykdomIFamilien("4487"),
        Vikafossen("2103");

        internal companion object {
            internal fun String.fraEnhetsnummer() = values().firstOrNull {
                it.nummer == this
            } ?: throw IllegalArgumentException("Finner ikke enhet med enhetsnummer $this")
        }
    }
}
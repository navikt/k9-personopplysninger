package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Enhetsnummer.Enhet.Companion.fraEnhetsnummer

internal object Enhetsnummer {
    private val tilSykdomIFamilien = setOf("UGRADERT", "FORTROLIG")

    internal fun String.adressebeskyttelseTilEnhetnummer() = when {
        this.toUpperCase() in tilSykdomIFamilien -> Enhet.SykdomIFamilien
        else -> Enhet.Viken
    }.nummer

    internal fun Collection<String>.fellesEnhetsnummer() = when (map { it.fraEnhetsnummer() }.any { it == Enhet.Viken }) {
        true -> Enhet.Viken.nummer
        false -> Enhet.SykdomIFamilien.nummer
    }

    internal enum class Enhet(internal val nummer: String) {
        SykdomIFamilien("4487"),
        Viken("2103");

        internal companion object {
            internal fun String.fraEnhetsnummer() = values().firstOrNull {
                it.nummer == this
            } ?: throw IllegalStateException("Finner ikke enhet med enhetsnummer $this")
        }
    }
}
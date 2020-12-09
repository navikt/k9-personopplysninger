package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Enhet.Companion.adressebeskyttelseTilEnhet
import no.nav.omsorgspenger.personopplysninger.Enhet.Companion.fellesEnhet
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class EnhetTest {

    @Test
    fun `Adressebeskyttelse til Enhet`() {
        "UGRADERT".adressebeskyttelseTilEnhet().assertSykdomIFamilien()
        "FORTROLIG".adressebeskyttelseTilEnhet().assertSykdomIFamilien()
        "FINNESIKKE".adressebeskyttelseTilEnhet().assertVikafossen()
        "STRENGT_FORTROLIG".adressebeskyttelseTilEnhet().assertVikafossen()
        "STRENGT_FORTROLIG_UTLAND".adressebeskyttelseTilEnhet().assertVikafossen()
    }

    @Test
    fun `Felles Enhet for enhetsnummer som ikke finnes`() {
        assertThrows(IllegalArgumentException::class.java) {
            listOf("1337","4487","2103").fellesEnhet()
        }
    }

    @Test
    fun `Felles Enhet for reelle enhetsnummer`() {
        listOf("4487","4487","4487","4487").fellesEnhet().assertSykdomIFamilien()
        listOf("4487","4487","2103","4487").fellesEnhet().assertVikafossen()
        listOf("4487").fellesEnhet().assertSykdomIFamilien()
        listOf("2103").fellesEnhet().assertVikafossen()
    }

    private fun Enhet.assertSykdomIFamilien() = assertEquals(Enhet.SykdomIFamilien, this)
    private fun Enhet.assertVikafossen() = assertEquals(Enhet.Vikafossen, this)
}
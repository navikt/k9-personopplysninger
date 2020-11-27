package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Enhetsnummer.adressebeskyttelseTilEnhetnummer
import no.nav.omsorgspenger.personopplysninger.Enhetsnummer.fellesEnhetsnummer
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class EnhetsnummerTest {

    @Test
    fun `Adressebeskyttelse til Enhetsnummer`() {
        "UGRADERT".adressebeskyttelseTilEnhetnummer().assertSykdomIFamilien()
        "FORTROLIG".adressebeskyttelseTilEnhetnummer().assertSykdomIFamilien()
        "FINNESIKKE".adressebeskyttelseTilEnhetnummer().assertVikafossen()
        "STRENGT_FORTROLIG".adressebeskyttelseTilEnhetnummer().assertVikafossen()
        "STRENGT_FORTROLIG_UTLAND".adressebeskyttelseTilEnhetnummer().assertVikafossen()
    }

    @Test
    fun `Felles Enhetsnummer for enhetsnummer som ikke finnes`() {
        assertThrows(IllegalArgumentException::class.java) {
            listOf("1337","4487","2103").fellesEnhetsnummer()
        }
    }

    @Test
    fun `Felles Enhetsnummer for reelle enhetsnummer`() {
        listOf("4487","4487","4487","4487").fellesEnhetsnummer().assertSykdomIFamilien()
        listOf("4487","4487","2103","4487").fellesEnhetsnummer().assertVikafossen()
        listOf("4487").fellesEnhetsnummer().assertSykdomIFamilien()
        listOf("2103").fellesEnhetsnummer().assertVikafossen()
    }

    private fun String.assertSykdomIFamilien() = assertEquals(Enhetsnummer.Enhet.SykdomIFamilien.nummer, this)
    private fun String.assertVikafossen() = assertEquals(Enhetsnummer.Enhet.Vikafossen.nummer, this)

}
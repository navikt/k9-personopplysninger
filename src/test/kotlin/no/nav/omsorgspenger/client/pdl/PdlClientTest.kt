package no.nav.omsorgspenger.client.pdl

import kotlinx.coroutines.runBlocking
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.testutils.ApplicationContextExtension
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApplicationContextExtension::class)
internal class PdlClientTest(
        applicationContext: ApplicationContext) {

    private val pdlClient = applicationContext.pdlClient

    @Test
    fun `Parser gyldig request`() {
        val response = runBlocking {
            pdlClient.getPersonInfo("01019911111")
        }
        assert(response.data.hentPerson.toString().contains("01011999"))
        assert(response.data.hentIdenter.toString().contains("01019911111"))
    }

    @Test
    fun `Svar utan løsning`() {
        val response = runBlocking {
            pdlClient.getPersonInfo("404")
        }
        assertNotNull(response.errors)
        assert(response.errors.toString().contains("unauthorized"))
    }

    @Test
    fun `Flera ident i input`() {
        val personer = setOf("12345678910", "12345678911")
        val response = runBlocking {
            pdlClient.getPersonInfo(personer)
        }
        assert(response.data.hentIdenterBolk?.size == 2)
    }

    @Test
    fun `Inget svar fra PDL kaster illegalstate exception`() {
        assertThrows(IllegalStateException::class.java) {
             runBlocking {
                pdlClient.getPersonInfo("500")
            }
        }

    }

}
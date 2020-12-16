package no.nav.omsorgspenger.personopplysninger.pdl

import kotlin.test.assertNull
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
            pdlClient.getPersonInfo(setOf("01019911111"), "testId")
        }
        assert(response.data.hentIdenterBolk.toString().contains("01019911111"))
    }

    @Test
    fun `Svar utan løsning`() {
        val response = runBlocking {
            pdlClient.getPersonInfo(setOf("404"), "test")
        }
        assertNotNull(response.errors)
        assert(response.errors.toString().contains("unauthorized"))
        assertNull(response.data.hentIdenterBolk)
    }

    @Test
    fun `Inget svar fra PDL kaster illegalstate exception`() {
        assertThrows(IllegalStateException::class.java) {
             runBlocking {
                pdlClient.getPersonInfo(setOf("500"), "testId")
            }
        }

    }

    @Test
    fun `Parser familierelasjoner`() {
        val response = runBlocking {
            pdlClient.hentFamilierelasjoner("111222333", "testId")
        }

        assert(response.data.hentPersonBolk.toString().contains("111222333"))
    }



}
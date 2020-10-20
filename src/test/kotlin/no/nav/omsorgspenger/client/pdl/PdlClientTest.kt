package no.nav.omsorgspenger.client.pdl

import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.testutils.ApplicationContextExtension
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
        assertNotNull(response.data.hentPerson)
    }

    @Test
    fun `Svar utan løsning`() {
        val response = runBlocking {
            pdlClient.getPersonInfo("404")
        }
        assertNotNull(response.errors)
    }

    @Test
    fun `Flera ident i input`() {
        val response = runBlocking {
            pdlClient.getPersonInfo(arrayOf("12345678910", "12345678911"))
        }
        assertNotNull(response.data.hentIdenterBolk)
        assertNotNull(response.data.hentPersonBolk)
    }

}
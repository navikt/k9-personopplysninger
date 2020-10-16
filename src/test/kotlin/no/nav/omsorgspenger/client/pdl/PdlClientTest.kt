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
            pdlClient.getPersonInfo("111111")
        }

        assertNotNull(response.data)
    }

}
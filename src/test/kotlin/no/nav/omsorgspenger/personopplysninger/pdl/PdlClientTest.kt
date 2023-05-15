package no.nav.omsorgspenger.personopplysninger.pdl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.testutils.ApplicationContextExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
    fun `Inget svar fra PDL kaster illegalargument exception`() {
        assertThrows(IllegalArgumentException::class.java){
             runBlocking {
                pdlClient.getPersonInfo(setOf("500"), "testId")
            }
        }

    }

    @Test
    fun `Parser flere identer`() {
        val response = runBlocking {
            pdlClient.HentRelasjonInfo(setOf("1234", "4321", "1111"), "testId")
        }

        assert(response.data.hentPersonBolk.toString().contains("1234"))
        assert(response.data.hentPersonBolk.toString().contains("4321"))
        assert(response.data.hentPersonBolk.toString().contains("1111"))
    }

    @Test
    fun `Håndterer ukjente meldinger og felter i errors response fra PDL`() {
        val response = runBlocking {
            pdlMock.getPersonInfo(setOf("11"), "feilTest1")
        }

        assertNotNull(response)
        assertEquals("ok", response.data.hentPersonBolk?.first()?.code)
    }


    private companion object {
        private val objectMapper = jacksonObjectMapper()

        private val pdlMock = mockk<PdlClient>().also {
            val feilTest1 = objectMapper.readValue(
                this::class.java.getResource("/pdl/test4-feilmeldinger.json").readText(Charsets.UTF_8),
                HentPdlResponse::class.java)

            coEvery { it.getPersonInfo(any(), eq("feilTest1")) }.returns(feilTest1)
        }
    }

}
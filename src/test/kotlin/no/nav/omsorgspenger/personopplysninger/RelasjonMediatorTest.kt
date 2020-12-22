package no.nav.omsorgspenger.personopplysninger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import org.junit.jupiter.api.Test

internal class RelasjonMediatorTest {
    private val pdlClient: PdlClient = pdlMock
    private val relasjonMediator = RelasjonMediator(pdlClient)

    @Test
    fun `Mor med barn + man med barn från tidigare førhållanden`() {
        val resultat = runBlocking {
            relasjonMediator.hentRelasjoner(
                    identitetsnummer = "08027622446",
                    til = setOf("24021350083", "29087623775"),
                    correlationId = "test1")
        }

        val barn = "{relasjon=BARN, identitetsnummer=24021350083, borSammen=true}"
        val far = "{relasjon=INGEN, identitetsnummer=29087623775, borSammen=false}"

        assert(resultat.toString().contains(barn)) { "Forventet: $barn i resultat: \n $resultat" }
        assert(resultat.toString().contains(far)) { "Forventet: $far i resultat: \n $resultat" }
    }


    private companion object {
        private val objectMapper = jacksonObjectMapper()

        private val pdlMock = mockk<PdlClient>().also {
            val test1 = objectMapper.readValue(
                this::class.java.getResource("/pdl/test1-familie.json").readText(Charsets.UTF_8),
                HentRelasjonPdlResponse::class.java)
            coEvery { it.HentRelasjonInfo(any(), correlationId = "test1") }.returns(test1)
        }


    }

}
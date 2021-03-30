package no.nav.omsorgspenger.personopplysninger

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
                correlationId = "familie1"
            )
        }

        val barn = "{relasjon=BARN, identitetsnummer=24021350083, borSammen=true}"
        val far = "{relasjon=INGEN, identitetsnummer=29087623775, borSammen=false}"

        assert(resultat.toString().contains(barn)) { "Forventet: $barn i resultat: \n $resultat" }
        assert(resultat.toString().contains(far)) { "Forventet: $far i resultat: \n $resultat" }
    }

    @Test
    fun `Mor med barn på delt bosted og far på kontaktadresse`() {
        val resultat = runBlocking {
            relasjonMediator.hentRelasjoner(
                identitetsnummer = "08027622446",
                til = setOf("24021350083", "29087623775"),
                correlationId = "familie2"
            )
        }

        val barn = "{relasjon=BARN, identitetsnummer=24021350083, borSammen=true}"
        val far = "{relasjon=INGEN, identitetsnummer=29087623775, borSammen=true}"

        assert(resultat.toString().contains(barn)) { "Forventet: $barn i resultat: \n $resultat" }
        assert(resultat.toString().contains(far)) { "Forventet: $far i resultat: \n $resultat" }
    }

    @Test
    fun `Mor med et barn på delt bosted, et på postadresse i fritt format og far på oppholdsadresse`() {
        val resultat = runBlocking {
            relasjonMediator.hentRelasjoner(
                identitetsnummer = "08027622446",
                til = setOf("24021350083", "24021350084", "29087623775"),
                correlationId = "familie3"
            )
        }

        val barn1 = "{relasjon=BARN, identitetsnummer=24021350083, borSammen=true}"
        val barn2 = "{relasjon=BARN, identitetsnummer=24021350084, borSammen=true}"
        val far = "{relasjon=INGEN, identitetsnummer=29087623775, borSammen=true}"

        assert(resultat.toString().contains(barn1)) { "Forventet: $barn1 i resultat: \n $resultat" }
        assert(resultat.toString().contains(barn2)) { "Forventet: $barn2 i resultat: \n $resultat" }
        assert(resultat.toString().contains(far)) { "Forventet: $far i resultat: \n $resultat" }
    }


    private companion object {
        private val objectMapper = jacksonObjectMapper()

        private val pdlMock = mockk<PdlClient>().also {
            val testFamilie1 = objectMapper.readValue(
                this::class.java.getResource("/pdl/test1-familie.json").readText(Charsets.UTF_8),
                HentRelasjonPdlResponse::class.java)
            val testFamilie2 = objectMapper.readValue(
                this::class.java.getResource("/pdl/test2-familie.json").readText(Charsets.UTF_8),
                HentRelasjonPdlResponse::class.java)
            val testFamilie3 = objectMapper.readValue(
                this::class.java.getResource("/pdl/test3-familie.json").readText(Charsets.UTF_8),
                HentRelasjonPdlResponse::class.java)

            coEvery { it.HentRelasjonInfo(any(), eq("familie1")) }.returns(testFamilie1)
            coEvery { it.HentRelasjonInfo(any(), eq("familie2")) }.returns(testFamilie2)
            coEvery { it.HentRelasjonInfo(any(), eq("familie3")) }.returns(testFamilie3)
        }
    }

}
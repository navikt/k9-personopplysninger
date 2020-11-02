package no.nav.omsorgspenger.personopplysninger

import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.registerApplicationContext
import no.nav.omsorgspenger.testutils.ApplicationContextExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApplicationContextExtension::class)
internal class HentPersonopplysningerTest(
        private val applicationContext: ApplicationContext) {

    private val rapid = TestRapid().apply {
        this.registerApplicationContext(applicationContext)
    }

    @BeforeEach
    internal fun reset() {
        rapid.reset()
    }

    @Test
    fun `River tar emot och løser gyldigt behov`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("01019911111"))
        rapid.sendTestMessage(behovssekvens)
        val løsninger = rapid.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"]
        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"LITEN","mellomnavn":null},"fødselsdato":"1990-07-04","adressebeskyttelse":"UGRADERT","aktørId":"2722577091065"}"""
        assertEquals(expectedJson, løsninger.get("01019911111").toString())
    }

    @Test
    fun `Hanterar http 500 fra PDL`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("500"))
        rapid.sendTestMessage(behovssekvens)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Skickar inte tom løsning vid svar fra PDL null-data`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("404"))
        rapid.sendTestMessage(behovssekvens)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Response fra PDL med en part utan svar`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("12345678910", "12345678911"))
        rapid.sendTestMessage(behovssekvens)

        val løsninger = rapid.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"]
        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"LITEN","mellomnavn":null},"fødselsdato":"1990-07-04","adressebeskyttelse":"UGRADERT","aktørId":"2722577091065"}"""

        assert(løsninger.size() == 1)
        assertEquals(expectedJson, løsninger.get("12345678910").toString())
        assertNull(løsninger.get("12345678911"))
    }

    @Test
    fun `Person utan aktørId och emptyList adressebeskyttelse`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("123123"))
        rapid.sendTestMessage(behovssekvens)

        val løsninger = rapid.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"]
        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"STOR","mellomnavn":"MELLAN"},"fødselsdato":"1999-01-01"}"""

        assert(løsninger.size() == 1)
        assertEquals(expectedJson, løsninger.get("123123").toString())
    }

    @Test
    fun `Sender bara med behovsattributer i behov`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("123123"), setOf("navn"))
        rapid.sendTestMessage(behovssekvens)

        val løsninger = rapid.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"]
        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"STOR","mellomnavn":"MELLAN"}}"""

        assert(løsninger.size() == 1)
        assertEquals(expectedJson, løsninger.get("123123").toString())
    }

    internal companion object {
        const val BEHOV = "HentPersonopplysninger"
    }


    private fun nyBehovsSekvens(
            ident: Set<String>,
            attributer: Set<String>? = setOf("navn", "fødselsdato", "adressebeskyttelse", "aktørId")
    ) = Behovssekvens(
            id = "01BX5ZZKBKACTAV9WEVGEMMVS0",
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                    Behov(
                            navn = BEHOV,
                            input = mapOf(
                                    "identitetsnummer" to ident,
                                    "attributter" to attributer
                            )
                    )
            )
    ).keyValue
}

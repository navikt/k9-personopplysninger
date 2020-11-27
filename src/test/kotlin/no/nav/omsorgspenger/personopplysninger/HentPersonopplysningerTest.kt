package no.nav.omsorgspenger.personopplysninger

import java.util.*
import kotlin.test.assertFalse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.registerApplicationContext
import no.nav.omsorgspenger.testutils.ApplicationContextExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert

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
    fun `River tar emot och løser fullständigt behov`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("01019911111"),
                setOf("navn", "fødselsdato", "adressebeskyttelse", "aktørId", "gjeldendeIdentitetsnummer"))
        rapid.sendTestMessage(behovssekvens)

        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"LITEN","mellomnavn":null},"fødselsdato":"1990-07-04","adressebeskyttelse":"UGRADERT","aktørId":"2722577091065","gjeldendeIdentitetsnummer":"01019911111"}"""

        assertEquals(expectedJson, rapid.hentLøsning("01019911111"))
    }

    @Test
    fun `River tar emot och løser behov om aktørId og enhetsnummer`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("01019911111"),
            setOf("aktørId", "enhetsnummer"))
        rapid.sendTestMessage(behovssekvens)

        """{"enhetsnummer":"4487","aktørId":"2722577091065"}""".assertJsonEquals(
            rapid.hentLøsning("01019911111")
        )
        """{"enhetsnummer":"4487"}""".assertJsonEquals(
            rapid.hentFellesopplysninger()
        )
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

        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"LITEN","mellomnavn":null},"fødselsdato":"1990-07-04","adressebeskyttelse":"UGRADERT","aktørId":"2722577091065"}"""

        assertEquals(expectedJson, rapid.hentLøsning("12345678910"))
        assertEquals(1, rapid.antalLøsninger())
    }

    @Test
    fun `Person utan aktørId och emptyList adressebeskyttelse`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("123123"), setOf("navn", "fødselsdato"))
        rapid.sendTestMessage(behovssekvens)

        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"STOR","mellomnavn":"MELLAN"},"fødselsdato":"1999-01-01"}"""

        assertEquals(expectedJson, rapid.hentLøsning("123123"))
    }

    @Test
    fun `Sender bara med behovsattributer i behov`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("123123"), setOf("navn"))
        rapid.sendTestMessage(behovssekvens)

        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"STOR","mellomnavn":"MELLAN"}}"""

        assertEquals(expectedJson, rapid.hentLøsning("123123"))
    }

    @Test
    fun `Sender med tom lösning på person utan alla behovsattribut`() {
        val (_, behovssekvens) = nyBehovsSekvens(setOf("21108424238"))
        rapid.sendTestMessage(behovssekvens)

        val resultat = rapid.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"].toString()

        assertFalse(resultat.contains("21108424238"))
    }

    @Test
    fun `Sender endast en lösning där en av två personer icke har personinfo og adressebeskyttelse tomt`() {
        val(_, behovssekvens) = nyBehovsSekvens(setOf("21108424239", "15098422273"))
        rapid.sendTestMessage(behovssekvens)

        val expectedJson = """{"navn":{"etternavn":"MASKIN","fornavn":"LITEN","mellomnavn":null},"fødselsdato":"1990-07-04","adressebeskyttelse":"UGRADERT","aktørId":"1168457597360"}"""

        assertEquals(expectedJson, rapid.hentLøsning("15098422273"))
        assertEquals(1, rapid.antalLøsninger())
    }

    internal companion object {
        const val BEHOV = "HentPersonopplysninger"
    }

    private fun TestRapid.hentFellesopplysninger(): String {
        return this.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["fellesopplysninger"].toString()
    }

    private fun TestRapid.hentLøsning(ident: String): String {
        return this.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"].get(ident).toString()
    }

    private fun TestRapid.antalLøsninger(): Int {
        return this.inspektør.message(0)["@løsninger"]["HentPersonopplysninger"]["personopplysninger"].size()
    }


    private fun String.assertJsonEquals(actual: String) = JSONAssert.assertEquals(this, actual, true)


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
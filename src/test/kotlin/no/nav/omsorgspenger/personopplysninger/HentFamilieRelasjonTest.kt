package no.nav.omsorgspenger.personopplysninger

import java.util.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.registerApplicationContext
import no.nav.omsorgspenger.testutils.ApplicationContextExtension
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert

@ExtendWith(ApplicationContextExtension::class)
internal class HentFamilieRelasjonTest(
        private val applicationContext: ApplicationContext) {

    private val rapid = TestRapid().apply {
        this.registerApplicationContext(applicationContext)
    }

    @BeforeEach
    internal fun reset() {
        rapid.reset()
    }

    @Test
    fun `Inkluderer bare barn fra PDL familierelasjon`() {
        val (_, behovssekvens) = nyBehovsSekvens(
                identitetsnummer = "111222333",
                til = setOf("1234", "1235", "4444", "5555"))
        rapid.sendTestMessage(behovssekvens)

        @Language("JSON")
        val expectedJson = """
                [{
                    "relasjon": "BARN",
                    "identitetsnummer": "1234",
                    "borSammen": false
                }]
        """.trimIndent()

        expectedJson.assertJsonEquals(rapid.hentLøsning())
        Assertions.assertEquals(1, rapid.antalLøsninger())
    }

    private fun TestRapid.hentLøsning(): String {
        return this.inspektør.message(0)["@løsninger"][HentFamilieRelasjon.BEHOV]["relasjoner"].toString()
    }

    private fun TestRapid.antalLøsninger(): Int {
        return this.inspektør.message(0)["@løsninger"][HentFamilieRelasjon.BEHOV]["relasjoner"].size()
    }

    private fun String.assertJsonEquals(actual: String) = JSONAssert.assertEquals(this, actual, true)

    private fun nyBehovsSekvens(
            identitetsnummer: String,
            til: Set<String>
    ) = Behovssekvens(
            id = "01BX5ZZKBKACTAV9WEVGEMMVS0",
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                    Behov(
                            navn = HentFamilieRelasjon.BEHOV,
                            input = mapOf(
                                    "identitetsnummer" to identitetsnummer,
                                    "til" to til
                            )
                    )
            )
    ).keyValue
}
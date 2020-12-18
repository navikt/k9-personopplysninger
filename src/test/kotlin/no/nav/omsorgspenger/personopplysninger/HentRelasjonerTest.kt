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
internal class HentRelasjonerTest(
        private val applicationContext: ApplicationContext) {

    private val rapid = TestRapid().apply {
        this.registerApplicationContext(applicationContext)
    }

    @BeforeEach
    internal fun reset() {
        rapid.reset()
    }

    @Test
    fun `Parser och inkluderer alle IDer fra request`() {
        val (_, behovssekvens) = nyBehovsSekvens(
                identitetsnummer = "1234",
                til = setOf("4321", "1111"))
        rapid.sendTestMessage(behovssekvens)

        @Language("JSON")
        val expectedJson = """
                [{
                    "relasjon": "FAR",
                    "identitetsnummer": "4321",
                    "borSammen": false
                },
                {
                    "relasjon": "BARN",
                    "identitetsnummer": "1111",
                    "borSammen": true
                }]
        """.trimIndent()

        expectedJson.assertJsonEquals(rapid.hentLøsning())
        Assertions.assertEquals(2, rapid.antalLøsninger())
    }

    private fun TestRapid.hentLøsning(): String {
        return this.inspektør.message(0)["@løsninger"][HentRelasjoner.BEHOV]["relasjoner"].toString()
    }

    private fun TestRapid.antalLøsninger(): Int {
        return this.inspektør.message(0)["@løsninger"][HentRelasjoner.BEHOV]["relasjoner"].size()
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
                            navn = HentRelasjoner.BEHOV,
                            input = mapOf(
                                    "identitetsnummer" to identitetsnummer,
                                    "til" to til
                            )
                    )
            )
    ).keyValue
}
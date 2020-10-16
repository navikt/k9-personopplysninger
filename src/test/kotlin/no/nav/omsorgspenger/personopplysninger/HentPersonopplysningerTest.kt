package no.nav.omsorgspenger.personopplysninger

import java.util.*
import kotlin.test.assertEquals
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
    fun `Løser behov`() {

        val (_, behovssekvens) = nyBehovsSekvens("111111111")
        rapid.sendTestMessage(behovssekvens)

        println(rapid.inspektør.message(0))

        assertEquals(1, rapid.inspektør.size)
    }

    internal companion object {
        const val BEHOV = "HentPersonopplysninger"
        const val IDENTITETSNUMMER = "@behov.$BEHOV.identitetsnummer"
    }

    private fun nyBehovsSekvens(
            ident: String,
    ) = Behovssekvens(
            id = "01BX5ZZKBKACTAV9WEVGEMMVS0",
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                    Behov(
                            navn = BEHOV,
                            input = mapOf(
                                    "identitetsnummer" to ident,
                            )
                    )
            )
    ).keyValue

}

package no.nav.omsorgspenger.personopplysninger.pdl

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PdlDomeneTest {

    @Test
    fun `hentPersonInfoQuery ger riktig output`() {
        val testQuery = hentPersonInfoQuery(setOf("123", "456"))
        val personInfoQuery = PdlDomeneTest::class.java.getResource("/pdl/hentPersonBolkInfo.graphql")
            .readText().replace("[\n\r]", "")

        assertEquals(testQuery.query, personInfoQuery)
        assert(testQuery.variables.identer.size == 2)
    }

    @Test
    fun `hentRelasjonInfoQuery ger riktig output`() {
        val testQuery = hentRelasjonInfoQuery(setOf("123", "456"))
        val relasjonInfoQuery = PdlDomeneTest::class.java.getResource("/pdl/hentRelasjonerBolkInfo.graphql")
            .readText().replace("[\n\r]", "")

        assertEquals(testQuery.query, relasjonInfoQuery)
        assert(testQuery.variables.identer.size == 2)
    }

}
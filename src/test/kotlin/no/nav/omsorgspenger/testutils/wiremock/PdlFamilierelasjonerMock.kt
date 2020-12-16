package no.nav.omsorgspenger.testutils.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.matching.AnythingPattern

private const val pdlApiBasePath = "/pdlapi-mock"
private const val pdlApiMockPath = "/"

private fun WireMockServer.stubPdlHentFamilieFarMedEttBarn(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("111222333")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                        {
                                            "data": {
                                                "hentPersonBolk": [
                                                    {
                                                        "ident": "111222333",
                                                        "person": {
                                                            "familierelasjoner": [
                                                                {
                                                                    "relatertPersonsIdent": "1234",
                                                                    "relatertPersonsRolle": "FAR",
                                                                    "minRolleForPerson": "BARN"
                                                                }
                                                            ]
                                                        },
                                                        "code": "ok"
                                                    }
                                                ]
                                            }
                                        }
                            """.trimIndent())
                    )
    )

    return this
}


internal fun WireMockServer.stubPdlFamilierelasjoner() = stubPdlHentFamilieFarMedEttBarn()
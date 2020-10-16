package no.nav.omsorgspenger.testutils.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.matching.AnythingPattern

private const val pdlApiBasePath = "/pdlapi-mock"
private const val pdlApiMockPath = "/"

private fun WireMockServer.stubPdlApiHentPerson(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withHeader("x-nav-apiKey", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.query"))
                    .willReturn(
                    WireMock.aResponse()
                            .withStatus(200)
                            .withBody("""
                                {
                                  "data": {
                                    "hentPerson": {
                                      "navn": [
                                        {
                                          "fornavn": "Ola",
                                          "mellomnavn": null,
                                          "etternavn": "Normann"
                                        }
                                      ],
                                      "adressebeskyttelse": [
                                        {
                                            "gradering": "UGRADERT"
                                        }
                                      ],
                                      "foedsel": [
                                        {
                                            "foedselsdato": "01011999"
                                        }
                                      ]
                                    },
                                    "hentIdenter": {
                                        "identer": [
                                            {
                                                "ident": "01019911111"
                                            }
                                        ]
                                    }
                                  }
                                }
                            """.trimIndent())
            )
    )

    return this
}

internal fun WireMockServer.stubPdlApi() = stubPdlApiHentPerson()
internal fun WireMockServer.pdlApiBaseUrl() = baseUrl() + pdlApiBasePath
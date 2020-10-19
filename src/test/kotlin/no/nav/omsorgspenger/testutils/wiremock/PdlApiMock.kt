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
                    .withRequestBody(matchingJsonPath("$.variables.ident", equalTo("01019911111")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
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

private fun WireMockServer.stubPdlApiHentPersonError(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withHeader("x-nav-apiKey", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.ident", equalTo("404")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                {
                                  "errors": [
                                    {
                                      "message": "Ikke tilgang til å se person",
                                      "locations": [
                                        {
                                          "line": 30,
                                          "column": 5
                                        }
                                      ],
                                      "path": [
                                        "hentPerson"
                                      ],
                                      "extensions": {
                                        "code": "unauthorized",
                                        "classification": "ExecutionAborted"
                                      }
                                    }
                                  ],
                                  "data": {
                                    "hentPerson": null
                                  }
                                }
                            """.trimIndent())
                    )
    )

    return this
}

internal fun WireMockServer.stubPdlApi() = stubPdlApiHentPerson().stubPdlApiHentPersonError()
internal fun WireMockServer.pdlApiBaseUrl() = baseUrl() + pdlApiBasePath
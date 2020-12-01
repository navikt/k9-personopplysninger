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
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("01019911111")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                    {
                                        "data": {
                                            "hentPersonBolk": [
                                                {
                                                    "ident": "01019911111",
                                                    "person": {
                                                        "navn": [
                                                            {
                                                                "fornavn": "LITEN",
                                                                "mellomnavn": null,
                                                                "etternavn": "MASKIN"
                                                            }
                                                        ],
                                                        "foedsel": [
                                                            {
                                                                "foedselsdato": "1990-07-04"
                                                            }
                                                        ],
                                                        "adressebeskyttelse": []
                                                    },
                                                    "code": "ok"
                                                }
                                            ],
                                            "hentIdenterBolk": [
                                                {
                                                    "ident": "01019911111",
                                                    "identer": [
                                                        {
                                                            "ident": "2722577091065",
                                                            "gruppe": "AKTORID"
                                                        },
                                                        {
                                                            "ident": "01019911111",
                                                            "gruppe": "FOLKEREGISTERIDENT"
                                                        }
                                                    ],
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

private fun WireMockServer.stubPdlApiHentAnnenPerson(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("01019011111")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                        {
                                            "data": {
                                                "hentPersonBolk": [
                                                    {
                                                        "ident": "01019011111",
                                                        "person": {
                                                            "navn": [
                                                                {
                                                                    "fornavn": "LITEN",
                                                                    "mellomnavn": null,
                                                                    "etternavn": "MASKIN"
                                                                }
                                                            ],
                                                            "foedsel": [
                                                                {
                                                                    "foedselsdato": "1990-07-04"
                                                                }
                                                            ],
                                                            "adressebeskyttelse": [
                                                                {
                                                                    "gradering": "UGRADERT"
                                                                }
                                                            ]
                                                        },
                                                        "code": "ok"
                                                    }
                                                ],
                                                "hentIdenterBolk": [
                                                    {
                                                        "ident": "01019011111",
                                                        "identer": [
                                                            {
                                                                "ident": "2722577091065",
                                                                "gruppe": "AKTORID"
                                                            }
                                                        ],
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

private fun WireMockServer.stubPdlApiPersonUtenPersonInfo(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("21108424238")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                        {
                                          "data": {
                                            "hentPersonBolk": [
                                              {
                                                "ident": "21108424238",
                                                "person": {
                                                  "navn": [],
                                                  "foedsel": [],
                                                  "adressebeskyttelse": []
                                                },
                                                "code": "ok"
                                              }
                                            ],
                                            "hentIdenterBolk": [
                                              {
                                                "ident": "21108424238",
                                                "identer": [
                                                  {
                                                    "ident": "21108424238",
                                                    "gruppe": "FOLKEREGISTERIDENT"
                                                  },
                                                  {
                                                    "ident": "1712931710644",
                                                    "gruppe": "AKTORID"
                                                  }
                                                ],
                                                "code": "ok"
                                              }
                                            ]
                                          }
                                        }
                                    """.trimIndent()
                                    )
                    )
    )

    return this
}

private fun WireMockServer.stubPdlApiToPersonerEnPersonUtenPersonInfo(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("21108424239")))
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("15098422273")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                        {
                                          "data": {
                                            "hentPersonBolk": [
                                              {
                                                "ident": "21108424239",
                                                "person": {
                                                  "navn": [],
                                                  "foedsel": [],
                                                  "adressebeskyttelse": []
                                                },
                                                "code": "ok"
                                              },
                                              {
                                                "ident": "15098422273",
                                                "person": {
                                                  "navn": [
                                                            {
                                                                "fornavn": "LITEN",
                                                                "mellomnavn": null,
                                                                "etternavn": "MASKIN"
                                                            }
                                                            ],
                                                            "foedsel": [
                                                                {
                                                                    "foedselsdato": "1990-07-04"
                                                                }
                                                            ],
                                                            "adressebeskyttelse": [
                                                                {
                                                                    "gradering": "UGRADERT"
                                                                }
                                                            ]
                                                },
                                                "code": "ok"
                                              }
                                            ],
                                            "hentIdenterBolk": [
                                              {
                                                "ident": "21108424239",
                                                "identer": [
                                                    {
                                                        "ident": "21108424239",
                                                        "gruppe": "FOLKEREGISTERIDENT"
                                                    },
                                                    {
                                                        "ident": "1712931710644",
                                                        "gruppe": "AKTORID"
                                                    }
                                                ],
                                                "code": "ok"
                                              },
                                                {
                                                    "ident": "15098422273",
                                                    "identer": [
                                                        {
                                                            "ident": "15098422273",
                                                            "gruppe": "FOLKEREGISTERIDENT"
                                                        },
                                                        {
                                                            "ident": "1168457597360",
                                                            "gruppe": "AKTORID"
                                                        }
                                                    ],
                                                    "code": "ok"
                                                }
                                            ]
                                          }
                                        }
                                    """.trimIndent()
                                    )
                    )
    )

    return this
}

private fun WireMockServer.stubPdlApiPartialInfo(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("123123")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                        {
                                          "data": {
                                            "hentPersonBolk": [
                                              {
                                                "ident": "123123",
                                                "person": {
                                                  "navn": [
                                                    {
                                                      "fornavn": "STOR",
                                                      "mellomnavn": "MELLAN",
                                                      "etternavn": "MASKIN"
                                                    }
                                                  ],
                                                  "foedsel": [
                                                    {
                                                      "foedselsdato": "1999-01-01"
                                                    }
                                                  ],
                                                  "adressebeskyttelse": []
                                                },
                                                "code": "ok"
                                              }
                                            ],
                                            "hentIdenterBolk": [
                                              {
                                                "ident": "123123",
                                                "identer": [],
                                                "code": "not_found"
                                              }
                                            ]
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
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("404")))
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
                                    "hentPersonBolk": null,
                                    "hentIdenterBolk": null
                                  }
                                }
                            """.trimIndent())
                    )
    )

    return this
}

private fun WireMockServer.stubPdlApiHentPersonBolk(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("12345678910")))
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("12345678911")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                        {
                                            "data": {
                                                "hentPersonBolk": [
                                                    {
                                                        "ident": "12345678910",
                                                        "person": {
                                                            "navn": [
                                                                {
                                                                    "fornavn": "LITEN",
                                                                    "mellomnavn": null,
                                                                    "etternavn": "MASKIN"
                                                                }
                                                            ],
                                                            "foedsel": [
                                                                {
                                                                    "foedselsdato": "1990-07-04"
                                                                },
                                                                {
                                                                    "foedselsdato": "1990-07-04"
                                                                },
                                                                {
                                                                    "foedselsdato": "1990-07-04"
                                                                }
                                                            ],
                                                            "adressebeskyttelse": [
                                                                {
                                                                    "gradering": "UGRADERT"
                                                                }
                                                            ]
                                                        },
                                                        "code": "ok"
                                                    },
                                                    {
                                                        "ident": "12345678911",
                                                        "person": null,
                                                        "code": "not_found"
                                                    }
                                                ],
                                                "hentIdenterBolk": [
                                                    {
                                                        "ident": "12345678910",
                                                        "identer": [
                                                            {
                                                                "ident": "2722577091065",
                                                                "gruppe": "AKTORID"
                                                            }
                                                        ],
                                                        "code": "ok"
                                                    },
                                                    {
                                                        "ident": "12345678911",
                                                        "identer": null,
                                                        "code": "not_found"
                                                    }
                                                ]
                                            }
                                        }
                            """.trimIndent())
                    )
    )

    return this
}

private fun WireMockServer.stubPdlApiServerErrorResponse(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("500")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(500)
                    )
    )

    return this
}

internal fun WireMockServer.stubPdlApi() = stubPdlApiHentPerson()
        .stubPdlApiHentAnnenPerson()
        .stubPdlApiHentPersonError()
        .stubPdlApiHentPersonBolk()
        .stubPdlApiServerErrorResponse()
        .stubPdlApiPartialInfo()
        .stubPdlApiPersonUtenPersonInfo()
        .stubPdlApiToPersonerEnPersonUtenPersonInfo()

internal fun WireMockServer.pdlApiBaseUrl() = baseUrl() + pdlApiBasePath
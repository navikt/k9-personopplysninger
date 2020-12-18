package no.nav.omsorgspenger.testutils.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.matching.AnythingPattern

private const val pdlApiBasePath = "/pdlapi-mock"
private const val pdlApiMockPath = "/"

private fun WireMockServer.stubPdlHentFamilieFarMedTvaBarn(): WireMockServer {
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
                                                                    "relatertPersonsRolle": "BARN",
                                                                    "minRolleForPerson": "FAR"
                                                                },
                                                                {
                                                                    "minRolleForPerson": "FAR"
                                                                }
                                                            ],
                                                            "bostedsadresse": [
                                                                {
                                                                    "vegadresse": {
                                                                        "adressenavn": "testveien"
                                                                    }
                                                                }
                                                            ],
                                                            "deltBosted": []
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

/*
Mors IDENT = 1234 (Bor sammen med Barn på matrikkeladresse = samme1111)
Fars IDENT = 4321 (har ikke bosted)
Barns Ident = 1111
*/
private fun WireMockServer.stubPdlHentFamilieFarOchMorMedEttBarn(): WireMockServer {
    WireMock.stubFor(
            WireMock.post(WireMock
                    .urlPathMatching(".*$pdlApiMockPath.*"))
                    .withHeader("Authorization", containing("Bearer"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Token", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("1234")))
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("4321")))
                    .withRequestBody(matchingJsonPath("$.variables.identer", containing("1111")))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                {
                                    "data": {
                                        "hentPersonBolk": [
                                            {
                                                "ident": "1234",
                                                "person": {
                                                    "familierelasjoner": [
                                                        {
                                                            "relatertPersonsIdent": "1111",
                                                            "relatertPersonsRolle": "BARN",
                                                            "minRolleForPerson": "MOR"
                                                        }
                                                    ],
                                                    "bostedsadresse": [
                                                        {
                                                            "matrikkeladresse": {
                                                                "matrikkelId": "samme1111"
                                                            }
                                                        }
                                                    ],
                                                    "deltBosted": []
                                                },
                                                "code": "ok"
                                            },
                                            {
                                                "ident": "4321",
                                                "person": {
                                                    "familierelasjoner": [
                                                        {
                                                            "relatertPersonsIdent": "1111",
                                                            "relatertPersonsRolle": "BARN",
                                                            "minRolleForPerson": "FAR"
                                                        }
                                                    ],
                                                    "bostedsadresse": [
                                                        {
                                                            "matrikkeladresse": null
                                                        }
                                                    ],
                                                    "deltBosted": []
                                                },
                                                "code": "ok"
                                            },
                                            {
                                                "ident": "1111",
                                                "person": {
                                                    "familierelasjoner": [
                                                        {
                                                            "relatertPersonsIdent": "4321",
                                                            "relatertPersonsRolle": "FAR",
                                                            "minRolleForPerson": "BARN"
                                                        },
                                                        {
                                                            "relatertPersonsIdent": "1234",
                                                            "relatertPersonsRolle": "MOR",
                                                            "minRolleForPerson": "BARN"
                                                        }
                                                    ],
                                                    "bostedsadresse": [ ],
                                                    "deltBosted": [
                                                        {
                                                            "matrikkeladresse": {
                                                                "matrikkelId": "samme1111"
                                                            }
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


internal fun WireMockServer.stubPdlFamilierelasjoner() =
        stubPdlHentFamilieFarMedTvaBarn()
        .stubPdlHentFamilieFarOchMorMedEttBarn()
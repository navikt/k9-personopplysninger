package no.nav.omsorgspenger.testutils.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.matching.AnythingPattern

private const val pdlApiMockPath = "/"

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
                                                    "forelderBarnRelasjon": [
                                                        {
                                                            "relatertPersonsIdent": "1111",
                                                            "relatertPersonsRolle": "BARN",
                                                            "minRolleForPerson": "MOR"
                                                        }
                                                    ],
                                                    "bostedsadresse": [
                                                        {
                                                            "vegadresse": {
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
                                                    "forelderBarnRelasjon": [
                                                        {
                                                            "relatertPersonsIdent": "1111",
                                                            "relatertPersonsRolle": "BARN",
                                                            "minRolleForPerson": "FAR"
                                                        }
                                                    ],
                                                    "bostedsadresse": [
                                                        {
                                                            "vegadresse": {}
                                                        }
                                                    ],
                                                    "deltBosted": []
                                                },
                                                "code": "ok"
                                            },
                                            {
                                                "ident": "1111",
                                                "person": {
                                                    "forelderBarnRelasjon": [
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
                                                            "vegadresse": {
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

private fun WireMockServer.stubPdlTvaPersonerEnNotFound(): WireMockServer {
    WireMock.stubFor(
        WireMock.post(WireMock
            .urlPathMatching(".*$pdlApiMockPath.*"))
            .withHeader("Authorization", containing("Bearer"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(matchingJsonPath("$.variables.identer", containing("77777")))
            .withRequestBody(matchingJsonPath("$.variables.identer", containing("88888")))
            .withRequestBody(matchingJsonPath("$.variables.identer", containing("99999")))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                                {
                                   "data":{
                                      "hentPersonBolk":[
                                         {
                                            "code":"not_found",
                                            "ident":"77777",
                                            "person":null
                                         },
                                         {
                                            "code":"ok",
                                            "ident":"88888",
                                            "person":{
                                               "forelderBarnRelasjon":[
                                                  {
                                                     "minRolleForPerson":"BARN",
                                                     "relatertPersonsRolle":"MOR",
                                                     "relatertPersonsIdent":"99999"
                                                  }
                                               ],
                                               "bostedsadresse":[
                                                  {
                                                     "vegadresse":{
                                                        "matrikkelId":null,
                                                        "adressenavn":"SKRUBBENESVEGEN"
                                                     }
                                                  }
                                               ],
                                               "deltBosted":[
                                               ]
                                            }
                                         },
{
                                            "code":"ok",
                                            "ident":"99999",
                                            "person":{
                                               "forelderBarnRelasjon":[
                                                  {
                                                     "minRolleForPerson":"MOR",
                                                     "relatertPersonsRolle":"BARN",
                                                     "relatertPersonsIdent":"77777"
                                                  }
                                               ],
                                               "bostedsadresse":[
                                                  {
                                                     "vegadresse":{
                                                        "matrikkelId":null,
                                                        "adressenavn":null
                                                     }
                                                  }
                                               ],
                                               "deltBosted":[
                                               ]
                                            }
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
    stubPdlHentFamilieFarOchMorMedEttBarn()
    .stubPdlTvaPersonerEnNotFound()
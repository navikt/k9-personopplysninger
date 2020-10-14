package no.nav.omsorgspenger.personopplysninger

import io.ktor.client.HttpClient
import no.nav.omsorgspenger.StsRestClient

internal class PersonopplysningerMediator(
        stsRestClient: StsRestClient,
        httpClient: HttpClient
) {

    fun hentPersonopplysninger(): Map<String, String> {
        return emptyMap()
    }

}
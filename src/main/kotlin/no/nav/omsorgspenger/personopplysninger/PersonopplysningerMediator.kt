package no.nav.omsorgspenger.personopplysninger

import no.nav.klage.clients.pdl.PdlClient

internal class PersonopplysningerMediator(
        internal val pdlClient: PdlClient
) {

    fun hentPersonopplysninger(): Map<String, String> {

        return emptyMap()
    }

}
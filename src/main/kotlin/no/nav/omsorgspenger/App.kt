package no.nav.omsorgspenger

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.omsorgspenger.config.Environment
import no.nav.omsorgspenger.config.readServiceUserCredentials
import no.nav.omsorgspenger.personopplysninger.HentPersonopplysninger

fun main() {
    RapidApplication.create(System.getenv()).apply {
        medAlleRivers()
    }.start()

}

internal fun RapidsConnection.medAlleRivers(
        env: Environment = System.getenv()) {

    RapidApplication.create(env).apply {
        HentPersonopplysninger(this)
    }.start()

}
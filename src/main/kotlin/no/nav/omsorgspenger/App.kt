package no.nav.omsorgspenger

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.k9.rapid.river.RapidsStateListener
import no.nav.omsorgspenger.personopplysninger.HentPersonopplysninger
import no.nav.omsorgspenger.personopplysninger.HentRelasjoner

fun main() {
    val applicationContext = ApplicationContext.Builder().build()
    RapidApplication.create(
        env = applicationContext.env,
        builder = { withKtorModule { k9Personopplysninger(applicationContext) }})
        .apply { registerApplicationContext(applicationContext) }
        .start()
}

internal fun RapidsConnection.registerApplicationContext(applicationContext: ApplicationContext) {
    HentPersonopplysninger(
        rapidsConnection = this,
        personopplysningerMediator = applicationContext.personopplysningerMediator
    )
    HentRelasjoner(
        rapidsConnection = this,
        relasjonMediator = applicationContext.relasjonMediator
    )
    register(object : RapidsConnection.StatusListener {
        override fun onStartup(rapidsConnection: RapidsConnection) {
            applicationContext.start()
        }

        override fun onShutdown(rapidsConnection: RapidsConnection) {
            applicationContext.stop()
        }
    })

    register(RapidsStateListener(onStateChange = { state -> applicationContext.rapidsState = state }))
}



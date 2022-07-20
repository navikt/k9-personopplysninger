package no.nav.omsorgspenger

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.k9.rapid.river.RapidsStateListener
import no.nav.omsorgspenger.personopplysninger.HentPersonopplysninger
import no.nav.omsorgspenger.personopplysninger.HentRelasjoner

fun main() {
    val applicationContext = ApplicationContext.Builder().build()
    RapidApplication.Builder(config = RapidApplication.RapidApplicationConfig.fromEnv(env = applicationContext.env))
        .withKtorModule { k9Personopplysninger(applicationContext) }
        .build()
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



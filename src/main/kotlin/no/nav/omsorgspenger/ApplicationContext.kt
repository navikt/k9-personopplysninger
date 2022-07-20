package no.nav.omsorgspenger

import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import no.nav.helse.dusseldorf.ktor.health.*
import java.net.URI
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.ClientSecretAccessTokenClient
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.k9.rapid.river.Environment
import no.nav.k9.rapid.river.RapidsStateListener
import no.nav.k9.rapid.river.hentRequiredEnv
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import no.nav.omsorgspenger.personopplysninger.RelasjonMediator
import no.nav.omsorgspenger.personopplysninger.HentRelasjoner
import no.nav.omsorgspenger.personopplysninger.HentPersonopplysninger
import no.nav.omsorgspenger.personopplysninger.PersonopplysningerMediator

fun main() {
    val applicationContext = ApplicationContext.Builder().build()
    RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(applicationContext.env))
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

internal fun Application.k9Personopplysninger(applicationContext: ApplicationContext) {
    install(ContentNegotiation) {
        jackson()
    }

    val healthService = HealthService(
        healthChecks = applicationContext.healthChecks.plus(object : HealthCheck {
            override suspend fun check(): Result {
                val currentState = applicationContext.rapidsState
                return when (currentState.isHealthy()) {
                    true -> Healthy("RapidsConnection", currentState.asMap)
                    false -> UnHealthy("RapidsConnection", currentState.asMap)
                }
            }
        })
    )

    HealthReporter(
        app = "k9-personopplysninger",
        healthService = healthService
    )

    routing {
        HealthRoute(healthService = healthService)
    }
}

internal class ApplicationContext(
    val env: Environment,
    val pdlClient: PdlClient,
    val personopplysningerMediator: PersonopplysningerMediator,
    val relasjonMediator: RelasjonMediator,
    val healthChecks: Set<HealthCheck>
) {
    internal var rapidsState = RapidsStateListener.RapidsState.initialState()

    internal fun start() {}
    internal fun stop() {}

    internal class Builder(
        var env: Environment? = null,
        var accessTokenClient: AccessTokenClient? = null,
        var pdlClient: PdlClient? = null,
        var personopplysningerMediator: PersonopplysningerMediator? = null,
        var relasjonMediator: RelasjonMediator? = null
    ) {

        internal fun build(): ApplicationContext {
            val benyttetEnv = env ?: System.getenv()

            val benyttetAccessTokenClient = accessTokenClient ?: ClientSecretAccessTokenClient(
                clientId = benyttetEnv.hentRequiredEnv("AZURE_APP_CLIENT_ID"),
                clientSecret = benyttetEnv.hentRequiredEnv("AZURE_APP_CLIENT_SECRET"),
                tokenEndpoint = URI(benyttetEnv.hentRequiredEnv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"))
            )
            val benyttetPdlClient = pdlClient ?: PdlClient(
                env = benyttetEnv,
                accessTokenClient = benyttetAccessTokenClient
            )

            val benyttetPersonopplysningerMediator = personopplysningerMediator ?: PersonopplysningerMediator(
                pdlClient = benyttetPdlClient
            )

            val benyttetRelasjonMediator = relasjonMediator ?: RelasjonMediator(
                pdlClient = benyttetPdlClient
            )

            return ApplicationContext(
                env = benyttetEnv,
                pdlClient = benyttetPdlClient,
                personopplysningerMediator = benyttetPersonopplysningerMediator,
                relasjonMediator = benyttetRelasjonMediator,
                healthChecks = setOf(
                    benyttetPdlClient
                )
            )
        }
    }
}
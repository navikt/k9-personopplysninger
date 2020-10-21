package no.nav.omsorgspenger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.omsorgspenger.client.StsRestClient
import no.nav.omsorgspenger.client.pdl.PdlClient
import no.nav.omsorgspenger.config.Environment
import no.nav.omsorgspenger.config.ServiceUser
import no.nav.omsorgspenger.config.readServiceUserCredentials
import no.nav.omsorgspenger.personopplysninger.HentPersonopplysninger
import no.nav.omsorgspenger.personopplysninger.PersonopplysningerMediator

fun main() {
    val applicationContext = ApplicationContext.Builder().build()
    RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(applicationContext.env))
            .withKtorModule { omsorgspengerPersonopplysninger(applicationContext) }
            .build()
            .apply { registerApplicationContext(applicationContext) }
            .start()
}

internal fun RapidsConnection.registerApplicationContext(applicationContext: ApplicationContext) {
    HentPersonopplysninger(
            rapidsConnection = this,
            personopplysningerMediator = applicationContext.personopplysningerMediator
    )
    register(object : RapidsConnection.StatusListener {
        override fun onStartup(rapidsConnection: RapidsConnection) {
            applicationContext.start()
        }

        override fun onShutdown(rapidsConnection: RapidsConnection) {
            applicationContext.stop()
        }
    })
}

internal fun Application.omsorgspengerPersonopplysninger(applicationContext: ApplicationContext) {
    install(ContentNegotiation) {
        jackson()
    }
    routing {
        HealthRoute(healthService = applicationContext.healthService)
    }
}

internal class ApplicationContext(
        val env: Environment,
        val serviceUser: ServiceUser,
        val httpClient: HttpClient,
        val stsRestClient: StsRestClient,
        val pdlClient: PdlClient,
        val personopplysningerMediator: PersonopplysningerMediator,
        val healthService: HealthService) {

    internal fun start() {}
    internal fun stop() {}

    internal class Builder(
            var env: Environment? = null,
            var serviceUser: ServiceUser? = null,
            var httpClient: HttpClient? = null,
            var stsRestClient: StsRestClient? = null,
            var pdlClient: PdlClient? = null,
            var personopplysningerMediator: PersonopplysningerMediator? = null) {
        internal fun build(): ApplicationContext {
            val benyttetEnv = env ?: System.getenv()
            val benyttetHttpClient = httpClient ?: HttpClient {
                install(JsonFeature) { serializer = JacksonSerializer(objectMapper) }
            }
            val benyttetServiceUser = serviceUser ?: serviceUser ?: readServiceUserCredentials()
            val benyttetStsRestClient = stsRestClient ?: StsRestClient(
                    env = benyttetEnv,
                    serviceUser = benyttetServiceUser,
                    httpClient = benyttetHttpClient
            )
            val benyttetPdlClient = pdlClient ?: PdlClient(
                    env = benyttetEnv,
                    stsRestClient = benyttetStsRestClient,
                    httpClient = benyttetHttpClient,
                    serviceUser = benyttetServiceUser
            )
            val benyttetPersonopplysningerMediator = personopplysningerMediator ?: PersonopplysningerMediator(
                    pdlClient = benyttetPdlClient
            )

            return ApplicationContext(
                    env = benyttetEnv,
                    serviceUser = benyttetServiceUser,
                    httpClient = benyttetHttpClient,
                    stsRestClient = benyttetStsRestClient,
                    pdlClient = benyttetPdlClient,
                    personopplysningerMediator = benyttetPersonopplysningerMediator,
                    healthService = HealthService(healthChecks = setOf(
                            benyttetStsRestClient,
                            benyttetPdlClient
                    ))
            )
        }

        private companion object {
            val objectMapper: ObjectMapper = jacksonObjectMapper()
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .registerModule(JavaTimeModule())
        }
    }
}
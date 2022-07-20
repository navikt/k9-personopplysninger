package no.nav.omsorgspenger

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.ClientSecretAccessTokenClient
import no.nav.k9.rapid.river.Environment
import no.nav.k9.rapid.river.RapidsStateListener
import no.nav.k9.rapid.river.hentRequiredEnv
import no.nav.omsorgspenger.personopplysninger.PersonopplysningerMediator
import no.nav.omsorgspenger.personopplysninger.RelasjonMediator
import no.nav.omsorgspenger.personopplysninger.pdl.PdlClient
import java.net.URI

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
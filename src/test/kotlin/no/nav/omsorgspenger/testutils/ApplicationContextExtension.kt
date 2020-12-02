package no.nav.omsorgspenger.testutils

import io.ktor.util.KtorExperimentalAPI
import java.net.URI
import no.nav.helse.dusseldorf.oauth2.client.ClientSecretAccessTokenClient
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2JwksUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2TokenUrl
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.config.ServiceUser
import no.nav.omsorgspenger.testutils.wiremock.pdlApiBaseUrl
import no.nav.omsorgspenger.testutils.wiremock.stubPdlApi
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

internal class ApplicationContextExtension : ParameterResolver {

    @KtorExperimentalAPI
    internal companion object {
        private val wireMockServer = WireMockBuilder()
                .withAzureSupport()
                .build()
                .stubPdlApi()

        private val applicationContextBuilder = ApplicationContext.Builder(
                env = mapOf(
                        "PDL_BASE_URL" to wireMockServer.pdlApiBaseUrl(),
                        "PROXY_SCOPES" to "test/.default"
                ).let {
                    if (wireMockServer != null) {
                        it.plus(
                                mapOf(
                                        "AZURE_V2_ISSUER" to Azure.V2_0.getIssuer(),
                                        "AZURE_V2_JWKS_URI" to (wireMockServer.getAzureV2JwksUrl()),
                                        "AZURE_APP_CLIENT_ID" to "k9-personopplysninger"
                                )
                        )
                    } else it
                },
                serviceUser = ServiceUser(
                        username = "foo",
                        password = "bar"
                ),
                accessTokenClient = ClientSecretAccessTokenClient(
                        clientId = "k9-personopplysninger",
                        clientSecret = "azureSecret",
                        tokenEndpoint = URI(wireMockServer.getAzureV2TokenUrl())
                )
        )

        private val applicationContext = applicationContextBuilder.build()

        init {
            Runtime.getRuntime().addShutdownHook(Thread {
                wireMockServer.stop()
            })
        }

        private val støttedeParametre = listOf(
                ApplicationContext::class.java
        )
    }

    @KtorExperimentalAPI
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return støttedeParametre.contains(parameterContext.parameter.type)
    }

    @KtorExperimentalAPI
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return when (parameterContext.parameter.type) {
            ApplicationContext::class.java -> applicationContext
            ApplicationContext.Builder::class.java -> applicationContextBuilder
            else -> wireMockServer
        }
    }
}
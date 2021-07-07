package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2TokenUrl
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.testutils.wiremock.pdlApiBaseUrl
import no.nav.omsorgspenger.testutils.wiremock.stubPdlApi
import no.nav.omsorgspenger.testutils.wiremock.stubPdlFamilierelasjoner
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

internal class ApplicationContextExtension : ParameterResolver {

    internal companion object {
        private val wireMockServer = WireMockBuilder()
                .withAzureSupport()
                .build()
                .stubPdlApi()
                .stubPdlFamilierelasjoner()

        private val applicationContextBuilder = ApplicationContext.Builder(
            env = mapOf(
                "PDL_BASE_URL" to wireMockServer.pdlApiBaseUrl(),
                "PDL_SCOPES" to "test/.default",
                "AZURE_APP_CLIENT_ID" to "k9-personopplysninger",
                "AZURE_APP_CLIENT_SECRET" to "azureSecret",
                "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to wireMockServer.getAzureV2TokenUrl()
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

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return støttedeParametre.contains(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return when (parameterContext.parameter.type) {
            ApplicationContext::class.java -> applicationContext
            ApplicationContext.Builder::class.java -> applicationContextBuilder
            else -> wireMockServer
        }
    }
}
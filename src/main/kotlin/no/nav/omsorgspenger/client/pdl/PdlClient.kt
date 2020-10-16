package no.nav.klage.clients.pdl

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.post
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.omsorgspenger.StsRestClient
import no.nav.omsorgspenger.config.Environment
import no.nav.omsorgspenger.config.ServiceUser
import no.nav.omsorgspenger.config.hentRequiredEnv
import org.slf4j.LoggerFactory

internal class PdlClient(
        private val env: Environment,
        private val stsRestClient: StsRestClient,
        private val serviceUser: ServiceUser,
        private val httpClient: HttpClient
) : HealthCheck {

    private val logger = LoggerFactory.getLogger(PdlClient::class.java)
    private val pdlBaseUrl = env.hentRequiredEnv("PDL_BASE_URL")
    private val apiKey = env.hentRequiredEnv("PDL_API_GW_KEY")


    suspend fun getPersonInfo(ident: String): HentPdlResponse {
        return httpClient.post<HttpStatement>("$pdlBaseUrl") {
            header(HttpHeaders.Authorization, "Bearer ${stsRestClient.token()}")
            header("Nav-Consumer-Token", "Bearer ${stsRestClient.token()}")
            header("Nav-Consumer-Id", serviceUser.username)
            header("TEMA", "OMS")
            header("x-nav-apiKey", apiKey)
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            body = hentPersonQuery(ident)
        }.receive()
    }

    override suspend fun check() = kotlin.runCatching {
        httpClient.options<HttpStatement>(pdlBaseUrl).execute().status
    }.fold(
            onSuccess = { statusCode ->
                when (HttpStatusCode.OK == statusCode) {
                    true -> Healthy("PdlClient", "OK")
                    false -> UnHealthy("PdlClient", "Feil: Mottok Http Status Code ${statusCode.value}")
                }
            },
            onFailure = {
                UnHealthy("PdlClient", "Feil: ${it.message}")
            }
    )
}

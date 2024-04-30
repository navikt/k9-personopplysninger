package no.nav.omsorgspenger.personopplysninger.pdl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpOptions
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPost
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.jsonBody
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.k9.rapid.river.Environment
import no.nav.k9.rapid.river.hentRequiredEnv
import org.slf4j.LoggerFactory

internal class PdlClient(
    env: Environment,
    accessTokenClient: AccessTokenClient
) : HealthCheck {

    private val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)
    private val pdlBaseUrl = env.hentRequiredEnv("PDL_BASE_URL")
    private val pdlScope = setOf(env.hentRequiredEnv("PDL_SCOPES"))

    suspend fun getPersonInfo(ident: Set<String>, correlationId: String): HentPdlResponse {
        val (httpStatusCode, response) = pdlBaseUrl.httpPost { builder ->
            builder.header(HttpHeaders.Authorization, getAuthorizationHeader())
            builder.header("Nav-Call-Id", correlationId)
            builder.header("TEMA", "OMS")
            builder.header("Behandlingsnummer", OMSORGSPENGER_RAMMEMELDING) 
            builder.accept(ContentType.Application.Json)
            builder.jsonBody(objectMapper.writeValueAsString(hentPersonInfoQuery(ident)))
        }.readTextOrThrow()

        require(httpStatusCode.isSuccess()) {
            "HTTP ${httpStatusCode.value} fra $pdlBaseUrl"
        }

        return objectMapper.readValue(response)

    }

    suspend fun HentRelasjonInfo(ident: Set<String>, correlationId: String): HentRelasjonPdlResponse {
        val (httpStatusCode, response) = pdlBaseUrl.httpPost { builder ->
            builder.header(HttpHeaders.Authorization, getAuthorizationHeader())
            builder.header("Nav-Call-Id", correlationId)
            builder.header("TEMA", "OMS")
            builder.header("Behandlingsnummer", OMSORGSPENGER_RAMMEMELDING) 
            builder.accept(ContentType.Application.Json)
            builder.jsonBody(objectMapper.writeValueAsString(hentRelasjonInfoQuery(ident)))
        }.readTextOrThrow()

        require(httpStatusCode.isSuccess()) {
            "HTTP ${httpStatusCode.value} fra $pdlBaseUrl"
        }

        return objectMapper.readValue(response)
    }

    private fun getAuthorizationHeader() = cachedAccessTokenClient.getAccessToken(pdlScope).asAuthoriationHeader()

    override suspend fun check() = pdlBaseUrl.httpOptions { builder ->
        builder.header(HttpHeaders.Authorization, getAuthorizationHeader())
    }.second.fold(
        onSuccess = {
            when (HttpStatusCode.OK == it.status) {
                true -> Healthy("PdlClient", "OK")
                false -> UnHealthy("PdlClient", "Feil: Mottok Http Status Code ${it.status.value}")
            }
        },
        onFailure = {
            UnHealthy("PdlClient", "Feil: ${it.message}")
        }
    )

    private companion object {
        // https://behandlingskatalog.intern.nav.no/process/purpose/PLEIE_OMSORGS_OG_OPPLAERINGSPENGER/4a1c9324-9c5e-4ddb-ac7f-c55d1dcd9736
        const val OMSORGSPENGER_RAMMEMELDING = "B142"
        
        private val secureLogger = LoggerFactory.getLogger("tjenestekall")
        private val objectMapper: ObjectMapper = jacksonObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())
    }
}

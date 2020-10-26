package no.nav.omsorgspenger.personopplysninger

import kotlin.reflect.full.memberProperties
import kotlinx.coroutines.runBlocking
import no.nav.omsorgspenger.client.pdl.HentPdlResponse
import no.nav.omsorgspenger.client.pdl.PdlClient
import org.slf4j.LoggerFactory

internal class PersonopplysningerMediator(
        internal val pdlClient: PdlClient
) {
    private val secureLogger = LoggerFactory.getLogger("tjenestekall")

    fun hentPersonopplysninger(identitetsnummer: String): Map<String, String> {

        lateinit var attributer: Map<String, String>
        runBlocking {
            try {
                val response = pdlClient.getPersonInfo(identitetsnummer)
                if (!response.errors.isNullOrEmpty()) {
                    secureLogger.error("Fann feil vid hent av data fra PDL:", response.errors)
                }
                attributer = response.toLøsning().asMap()
            } catch (cause: Throwable) {
                throw IllegalStateException("Feil vid hent av data fra PDL:", cause)
            }

        }
        return attributer
    }

    private fun HentPdlResponse.toLøsning(): PersonInfo {
        requireNotNull(this.data.hentIdenter?.identer) { "Ident kan ikke vara null."}
        requireNotNull(this.data.hentPerson?.navn) { "Navn kan ikke vara null." }
        return PersonInfo(
                navn = this.data.hentPerson!!.navn[0].toString(),
                fødseldato = this.data.hentPerson?.foedsel?.get(0).foedselsdato,
                aktørId = this.data.hentIdenter!!.identer[0].ident
        )
    }

    private companion object {
        data class PersonInfo(
                val navn: String,
                val fødseldato: String?,
                val aktørId: String
        )

        inline fun <reified T : Any> T.asMap(): Map<String, String> {
            val props = T::class.memberProperties.associateBy { it.name }
            return props.keys.associateWith { props[it]?.get(this).toString() }
        }
    }
}
package no.nav.omsorgspenger.personopplysninger

import kotlin.reflect.full.memberProperties
import kotlinx.coroutines.runBlocking
import no.nav.klage.clients.pdl.PdlClient

internal class PersonopplysningerMediator(
        internal val pdlClient: PdlClient
) {

    fun hentPersonopplysninger(ident: String): Map<String, String> {

        var result = mutableMapOf<String, String>()

        runBlocking {
            val response = pdlClient.getPersonInfo(ident)

            for((k, v) in response.data.hentPerson!!.asMap()) {
                result.put(k, v.toString())
            }

            for((k, v) in response.data.hentIdenter!!.asMap()) {
                result.put(k, v.toString())
            }
        }

        return result.toMap()
    }


}

inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
    val props = T::class.memberProperties.associateBy { it.name }
    return props.keys.associateWith { props[it]?.get(this) }
}

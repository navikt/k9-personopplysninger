package no.nav.omsorgspenger.client.pdl

data class GraphqlQuery(
        val query: String,
        val variables: Variables
)

data class PersonInfoGraphqlQuery(
        val query: String,
        val variables: Variabler
)

data class Variabler(
        val ident: String
)

data class Variables(
        val ident: String
)

data class PdlError(
        val message: String,
        val locations: List<PdlErrorLocation>,
        val path: List<String>?,
        val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
        val line: Int?,
        val column: Int?
)

data class PdlErrorExtension(
        val code: String?,
        val classification: String
)

fun hentPersonInfoQuery(fnr: String): PersonInfoGraphqlQuery {
    val query = GraphqlQuery::class.java.getResource("/pdl/hentPersonInfo.graphql").readText().replace("[\n\r]", "")
    return PersonInfoGraphqlQuery(query, Variabler(fnr))
}

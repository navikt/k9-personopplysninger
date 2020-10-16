package no.nav.klage.clients.pdl

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

data class GraphqlQuery(
    val query: String,
    val variables: Variables
)

data class Variables(
        val ident: String
)

fun hentPersonQuery(identitetsnummer: String): GraphqlQuery {
    val query = GraphqlQuery::class.java.getResource("/pdl/hentPersonInfo.graphql").readText().replace("[\n\r]", "")
    return GraphqlQuery(query, Variables(identitetsnummer))
}

package no.nav.klage.clients.pdl

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

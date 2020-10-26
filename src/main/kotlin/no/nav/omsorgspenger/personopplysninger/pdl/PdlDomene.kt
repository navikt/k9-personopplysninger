package no.nav.omsorgspenger.personopplysninger.pdl

data class GraphqlQuery(
        val query: String,
        val variables: Variables
)

data class BolkPersonInfoGraphqlQuery(
        val query: String,
        val variables: Variables
)

data class PersonInfoGraphqlQuery(
        val query: String,
        val variables: Variable
)

data class Variables(
        val identer: List<String>
)

data class Variable(
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
    return PersonInfoGraphqlQuery(query, Variable(fnr))
}

fun hentPersonInfoQuery(fnr: Set<String>): BolkPersonInfoGraphqlQuery {
    val query = GraphqlQuery::class.java.getResource("/pdl/hentPersonBolkInfo.graphql").readText().replace("[\n\r]", "")
    return BolkPersonInfoGraphqlQuery(query, Variables(fnr.toList()))
}
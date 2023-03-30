package no.nav.omsorgspenger.personopplysninger.pdl

import no.nav.omsorgspenger.personopplysninger.pdl.Queries.personInfoQuery
import no.nav.omsorgspenger.personopplysninger.pdl.Queries.relasjonInfoQuery

data class GraphqlQuery(
        val query: String,
        val variables: Variables
)

data class PersonInfoGraphqlQuery(
        val query: String,
        val variables: Variables
)

data class Variables(
        val identer: List<String>
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
        val classification: String?,
        val details: String?
)

fun hentPersonInfoQuery(fnr: Set<String>): PersonInfoGraphqlQuery {
    return PersonInfoGraphqlQuery(personInfoQuery, Variables(fnr.toList()))
}

fun hentRelasjonInfoQuery(fnr: Set<String>): PersonInfoGraphqlQuery {
    return PersonInfoGraphqlQuery(relasjonInfoQuery, Variables(fnr.toList()))
}

private object Queries {
    @JvmStatic
    val personInfoQuery = GraphqlQuery::class.java.getResource("/pdl/hentPersonBolkInfo.graphql").readText().replace("[\n\r]", "")
    @JvmStatic
    val relasjonInfoQuery = GraphqlQuery::class.java.getResource("/pdl/hentRelasjonerBolkInfo.graphql").readText().replace("[\n\r]", "")
}
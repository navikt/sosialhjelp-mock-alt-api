package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model

data class HentPersonRequest(val query: String, val variables: Variables)

data class Variables(val ident: String)

data class HentGeografiskTilknytningRequest(val query: String, val variables: Variables)

data class MockGraphQLRequest(val query: String, val variables: Map<String, Any>)

data class SokAdresseRequest(val query: String, val variables: SokAdresseVariables)

data class SokAdresseVariables(val paging: Paging, val criteria: List<Criteria>)

data class Paging(val pageNumber: Int, val resultsPerPage: Int, val sortBy: List<SortBy>)

data class SortBy(val fieldName: String, val direction: String)

data class Criteria(val fieldName: String, val searchRule: Map<String, String>)

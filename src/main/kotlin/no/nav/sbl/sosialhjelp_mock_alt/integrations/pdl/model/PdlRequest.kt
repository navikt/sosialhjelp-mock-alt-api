package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model

data class PdlRequest(
        val query: String,
        val variables: Variables
)

data class Variables(
        val ident: String
)

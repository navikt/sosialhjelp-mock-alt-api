package no.nav.sbl.sosialhjelp_mock_alt.integrations.azure.model

data class AzureAdBruker(
    val id: String,
    val userPrincipalName: String,
    val givenName: String,
    val surname: String
)

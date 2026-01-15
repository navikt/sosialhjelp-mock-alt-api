package no.nav.sbl.sosialhjelp.mock.alt.integrations.azure.model

data class AzureAdGruppe(
    val id: String,
    val onPremisesSamAccountName: String?,
)

class AzureAdGrupper(
    val value: List<AzureAdGruppe>,
)

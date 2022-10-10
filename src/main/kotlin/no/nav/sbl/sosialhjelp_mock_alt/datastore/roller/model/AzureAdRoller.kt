package no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.model

data class AzureAdRoller(
    val roller: List<AdminRolle>
)

enum class AdminRolle {
    MODIA_VEILEDER,
}

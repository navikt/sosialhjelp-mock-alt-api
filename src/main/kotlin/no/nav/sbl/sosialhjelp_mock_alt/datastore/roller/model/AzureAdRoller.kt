package no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.model

data class AzureAdRoller(
    val roller: List<AdminRolle>
)

enum class AdminRolle {
    DIALOG_VEILEDER,
    DIALOG_ADMINISTRATOR,
    DIALOG_TEKNISK_ARKIV,
    DIALOG_INNSIKKT,
    MODIA_VEILEDER,
}

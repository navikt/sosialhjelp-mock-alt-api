package no.nav.sbl.sosialhjelp.mock.alt.datastore.roller.model

data class AzureAdRoller(
    val roller: List<AdminRolle>,
)

enum class AdminRolle {
    MODIA_VEILEDER,
}

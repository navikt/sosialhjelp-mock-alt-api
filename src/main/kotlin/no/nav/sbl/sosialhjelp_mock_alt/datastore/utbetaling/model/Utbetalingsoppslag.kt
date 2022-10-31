package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.Periode

data class Utbetalingsoppslag(
    val ident: String,
    val rolle: Rolle,
    val periode: Periode,
    val periodetype: Periodetype,
)

enum class Rolle(val databaseverdi: String) {
    UTBETALT_TIL("UtbetaltTil"),
    RETTIGHETSHAVER("Rettighetshaver")
}

enum class Periodetype(val databaseverdi: String) {
    UTBETALINGSPERIODE("Utbetalingsperiode"),
    YTELSESPERIODE("Ytelsesperiode")
}

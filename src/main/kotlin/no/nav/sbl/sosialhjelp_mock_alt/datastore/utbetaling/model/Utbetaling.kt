package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model

import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigOrganisasjonsnummer
import java.time.LocalDate

data class UtbetalingDto(
    val type: String = "navytelse",
    val netto: Double = 1337.0,
    val brutto: Double = 2000.0,
    val skattetrekk: Double = 600.0,
    val andreTrekk: Double = 63.0,
    val bilagsnummer: String? = "bilagsnummer",
    val utbetalingsdato: LocalDate? = LocalDate.now(),
    val periodeFom: LocalDate? = LocalDate.now().minusDays(14),
    val periodeTom: LocalDate? = LocalDate.now().minusDays(2),
    val komponenter: List<KomponentDto> = listOf(KomponentDto()),
    val tittel: String = "Dagpenger",
    val orgnummer: String = genererTilfeldigOrganisasjonsnummer()
)

data class KomponentDto(
    val type: String? = "type",
    val belop: Double = 42.0,
    val satstype: String? = "satstype",
    val satsbelop: Double = 21.0,
    val satsantall: Double = 2.0
)
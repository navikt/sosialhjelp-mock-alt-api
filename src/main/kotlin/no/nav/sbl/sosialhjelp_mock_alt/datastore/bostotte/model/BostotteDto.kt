@file:Suppress("unused")

package no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model

import java.time.LocalDate
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt

data class BostotteDto(
    val saker: MutableList<SakerDto> = mutableListOf(),
    val utbetalinger: MutableList<UtbetalingerDto> = mutableListOf(),
) {
  fun withSak(sak: SakerDto) {
    saker.add(sak)
  }

  fun withUtbetaling(utbetaling: UtbetalingerDto) {
    utbetalinger.add(utbetaling)
  }
}

data class SakerDto(
    val mnd: Int,
    val ar: Int,
    val status: BostotteStatus,
    val vedtak: VedtakDto? = null,
    val rolle: BostotteRolle = BostotteRolle.HOVEDPERSON,
)

enum class BostotteStatus {
  UNDER_BEHANDLING,
  VEDTATT
}

enum class BostotteRolle {
  HOVEDPERSON,
  BIPERSON
}

data class VedtakDto(
    val kode: String,
    val beskrivelse: String,
    val type: String,
)

data class UtbetalingerDto(
    val belop: Double = randomInt(5).toDouble(),
    val utbetalingsdato: LocalDate,
    val mottaker: BostotteMottaker = BostotteMottaker.HUSSTAND,
    val rolle: BostotteRolle = BostotteRolle.HOVEDPERSON,
)

enum class BostotteMottaker(val value: String) {
  KOMMUNE(JsonOkonomiOpplysningUtbetaling.Mottaker.KOMMUNE.value()),
  HUSSTAND(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND.value()),
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import java.time.LocalDate

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
        val rolle: BostotteRolle? = BostotteRolle.HOVEDPERSON,
)

enum class BostotteStatus { UNDER_BEHANDLING, VEDTATT }
enum class BostotteRolle { HOVEDPERSON, BIPERSON }

data class VedtakDto(val vedtak: Vedtakskode) {
    val kode: String
    val beskrivelse: String
    val type: String

    init {
        kode = vedtak.name
        beskrivelse = vedtak.beskrivelse
        type = vedtak.type
    }

}

enum class Vedtakskode(val beskrivelse: String, val type: String) {
    V00("Søknaden din er innvilget.", "INNVILGET"),
    V02("Du har fått avslag på søknaden din om bostøtte fordi du eller andre i husstanden ikke har rett til bostøtte.", "AVSLAG"),
    V03("Du har fått avslag på søknaden din om bostøtte fordi du/dere hadde for høy inntekt.", "AVSLAG"),
    V04("Du har fått avslag på søknaden din om bostøtte fordi boligen din ikke oppfyller kravene.", "AVSLAG"),
    V05("Du har fått avslag på søknaden din om bostøtte fordi du ikke var registrert på søknadsadressen i folkeregisteret.", "AVSLAG"),
    V07("Klagen din ble avvist da den ble sendt inn etter klagefristen.", "AVVIST"),
    V09(" Søknaden din om bostøtte er avvist fordi det mangler opplysninger eller dokumentasjon, eller fordi noen i husstanden er registrert på en annen søknad.", "AVVIST"),
    V11("Hovedperson er død", "AVSLAG"),
    V12("For høy anslått inntekt (ikke i bruk lengere)", "AVSLAG"),
    V48("Søknaden din om bostøtte er avvist fordi noen du bor sammen med ikke er registrert på adressen i folkeregisteret.", "AVSLAG"),
}

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


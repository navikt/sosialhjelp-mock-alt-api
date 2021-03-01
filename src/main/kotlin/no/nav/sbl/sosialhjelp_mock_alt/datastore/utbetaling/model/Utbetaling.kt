package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model

import java.time.LocalDate

data class UtbetalingsListeDto(
        var utbetalinger: List<UtbetalingDto> = listOf(),
) {
    fun add(utbetalingDto: UtbetalingDto): UtbetalingsListeDto {
        val mutableList = mutableListOf<UtbetalingDto>()
        mutableList.addAll(utbetalinger)
        mutableList.add(utbetalingDto)
        utbetalinger = mutableList
        return this
    }
}

data class UtbetalingDto(
        val belop: Double = 1337.0,
        val dato: LocalDate = LocalDate.now(),
        val ytelsestype: String = "Dagpenger",
        val melding: String = "",
        val skattebelop: Double = 0.0,
        val ytelseskomponenttype: String = "",
)

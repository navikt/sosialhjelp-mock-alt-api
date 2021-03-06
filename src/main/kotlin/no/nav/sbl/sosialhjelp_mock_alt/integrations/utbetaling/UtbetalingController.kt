package no.nav.sbl.sosialhjelp_mock_alt.integrations.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalingService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingerResponseDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UtbetalingController(
    private val utbetalingService: UtbetalingService,
    private val feilService: FeilService,
) {

    @GetMapping("/oppslag-api/utbetalinger")
    fun getUtbetalingerFraNav(
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<UtbetalingerResponseDto> {
        val ident = hentFnrFraToken(headers)
        feilService.eventueltLagFeil(ident, "UtbetalingController", "getUtbetalingerFraNav")
        val utbetalinger = utbetalingService.getUtbetalingerFraNav(ident)
        val response = UtbetalingerResponseDto(utbetalinger, false)
        log.info("Henter utbetalinger fra nav: ${objectMapper.writeValueAsString(utbetalinger)}")
        return ResponseEntity.ok(response)
    }

    companion object {
        private val log by logger()
    }
}

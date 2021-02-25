package no.nav.sbl.sosialhjelp_mock_alt.integrations.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalingService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingsListeDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UtbetalingController(
        private val utbetalingService: UtbetalingService,
        private val feilService: FeilService,
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/utbetaling")
    fun getStatteetatenInntekt(
            @RequestParam fnr: String,
): ResponseEntity<UtbetalingsListeDto> {
        feilService.eventueltLagFeil(fnr, "UtbetalingController", "getStatteetatenInntekt")
        val utbetalinger = utbetalingService.getUtbetalingerFraNav(fnr)
        log.info("Henter utbetalinger fra nav: ${objectMapper.writeValueAsString(utbetalinger)}")
        return ResponseEntity.ok(utbetalinger)
    }
}

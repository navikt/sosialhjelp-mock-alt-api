package no.nav.sbl.sosialhjelp_mock_alt.integrations.husbanken

import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteRolle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteStatus
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.SakerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.UtbetalingerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.VedtakDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.Vedtakskode
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class HusbankenController {
    companion object {
        private val log by logger()
    }

    @GetMapping("/husbanken")
    fun getHusbankenData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fra: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) til: LocalDate,
            @RequestHeader headers: HttpHeaders,
    ):
            ResponseEntity<BostotteDto> {
        val bostotte = BostotteDto()
        val dato1 = LocalDate.now().minusDays(15)
        val dato2 = LocalDate.now().minusDays(45)
        bostotte.withSak(SakerDto(
                dato1.monthValue + 1,
                dato1.year,
                BostotteStatus.VEDTATT,
                VedtakDto(Vedtakskode.V00)
        ))
        bostotte.withSak(SakerDto(
                dato2.monthValue + 1,
                dato2.year,
                BostotteStatus.VEDTATT,
                VedtakDto(Vedtakskode.V02),
                BostotteRolle.BIPERSON
        ))
        bostotte.withUtbetaling(UtbetalingerDto(randomInt(5).toDouble(), dato1))
        bostotte.withUtbetaling(UtbetalingerDto(randomInt(5).toDouble(), dato2))
        log.info("Henter husbanken bostotte:\n${objectMapper.writeValueAsString(bostotte)}")
        return ResponseEntity.ok(bostotte)
    }
}

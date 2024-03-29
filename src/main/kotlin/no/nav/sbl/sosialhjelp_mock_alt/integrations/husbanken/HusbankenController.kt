package no.nav.sbl.sosialhjelp_mock_alt.integrations.husbanken

import java.time.LocalDate
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HusbankenController(
    private val bostotteService: BostotteService,
    private val feilService: FeilService,
) {
  companion object {
    private val log by logger()
  }

  @GetMapping("/husbanken")
  fun getHusbankenData(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fra: LocalDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) til: LocalDate,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<BostotteDto> {
    val fnr = hentFnrFraToken(headers)
    feilService.eventueltLagFeil(fnr, "HusbankenController", "getHusbankenData")
    val bostotte = bostotteService.getBostotte(fnr)
    log.info("Henter husbanken bostotte:\n${objectMapper.writeValueAsString(bostotte)}")
    return ResponseEntity.ok(bostotte)
  }
}

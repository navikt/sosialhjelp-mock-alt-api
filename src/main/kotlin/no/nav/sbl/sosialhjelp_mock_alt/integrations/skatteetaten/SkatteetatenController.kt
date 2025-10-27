package no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SkatteetatenController(
    private val skatteetatenService: SkatteetatenService,
    private val feilService: FeilService,
) {
  companion object {
    private val log by logger()
  }

  @GetMapping("/skatteetaten/{fnr}/inntekter")
  fun getStatteetatenInntekt(
      @PathVariable fnr: String,
      @RequestParam fraOgMed: String,
      @RequestParam tilOgMed: String,
  ): ResponseEntity<SkattbarInntekt> {
    feilService.eventueltLagFeil(fnr, "SkatteetatenController", "getStatteetatenInntekt")
    val skattbarInntekt = skatteetatenService.getSkattbarInntekt(fnr)
    log.info("Henter skattbar inntekt: ${objectMapper.writeValueAsString(skattbarInntekt)}")
    return ResponseEntity.ok(skattbarInntekt)
  }
}

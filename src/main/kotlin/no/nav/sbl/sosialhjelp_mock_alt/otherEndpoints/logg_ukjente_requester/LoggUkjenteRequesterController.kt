package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.logg_ukjente_requester

import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.context.annotation.Profile
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("no-swagger")
class LoggUkjenteRequesterController {
  companion object {
    private val log by logger()
  }

  @RequestMapping("/**")
  fun loggUkjentRequest(request: RequestEntity<String>): ResponseEntity<String> {
    log.debug("Ukjent URL i request: ${request.url}\n${objectMapper.writeValueAsString(request)}")
    return ResponseEntity.notFound().build()
  }
}

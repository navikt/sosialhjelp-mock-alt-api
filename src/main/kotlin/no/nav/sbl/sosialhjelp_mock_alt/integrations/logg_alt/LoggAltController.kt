package no.nav.sbl.sosialhjelp_mock_alt.integrations.logg_alt

import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LoggAltController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/**")
    fun isAlive(request: RequestEntity<String>): ResponseEntity<String> {
        log.debug("Ukjent URL i request: ${request.url}\n${objectMapper.writeValueAsString(request)}")
        return ResponseEntity.notFound().build()
    }
}

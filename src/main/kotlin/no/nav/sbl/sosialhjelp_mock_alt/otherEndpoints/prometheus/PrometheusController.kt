package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.prometheus

import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PrometheusController {
    @RequestMapping("/internal/prometheus")
    fun fakePrometheus(request: RequestEntity<String>): ResponseEntity<String> {
        return ResponseEntity.ok().build()
    }
}

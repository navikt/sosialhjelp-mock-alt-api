package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.unleash

import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UnleashController {
    @GetMapping("/internal/unleash/client/features")
    fun unleashToggles(request: RequestEntity<String>): ResponseEntity<String> {
        return ResponseEntity.ok("{\"features\": [], \"version\": \"1\"}")
    }

    @PostMapping("/internal/unleash/client/register")
    fun unleashRegister(request: RequestEntity<String>): ResponseEntity<String> {
        return ResponseEntity.ok().build()
    }

    @PostMapping("/internal/unleash/client/metrics")
    fun unleashMetrics(request: RequestEntity<String>): ResponseEntity<String> {
        return ResponseEntity.ok().build()
    }
}

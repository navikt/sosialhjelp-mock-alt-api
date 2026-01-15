package no.nav.sbl.sosialhjelp.mock.alt.otherEndpoints.unleash

import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UnleashController {
    @GetMapping("/internal/unleash/api/client/features")
    fun unleashToggles(request: RequestEntity<String>): ResponseEntity<String> = ResponseEntity.ok("{\"features\": [], \"version\": \"1\"}")

    @PostMapping("/internal/unleash/api/client/register")
    fun unleashRegister(request: RequestEntity<String>): ResponseEntity<String> = ResponseEntity.ok().build()

    @PostMapping("/internal/unleash/api/client/metrics")
    fun unleashMetrics(request: RequestEntity<String>): ResponseEntity<String> = ResponseEntity.ok().build()
}

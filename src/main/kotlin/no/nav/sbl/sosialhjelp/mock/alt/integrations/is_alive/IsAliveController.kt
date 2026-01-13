package no.nav.sbl.sosialhjelp.mock.alt.integrations.isalive

import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IsAliveController {
    @GetMapping("/internal/isAlive")
    fun isAlive(
        @RequestParam parameters: MultiValueMap<String, String>,
    ): String = "OK"

    @GetMapping("/internal/isReady")
    fun isReady(
        @RequestParam parameters: MultiValueMap<String, String>,
    ): String = "OK"
}

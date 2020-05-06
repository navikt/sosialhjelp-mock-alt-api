package no.nav.sbl.sosialhjelp_mock_alt.integrations.is_alive

import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IsAliveController {
    @RequestMapping("/sosialhjelp/mock-alt-api/internal/isAlive")
    fun isAlive(@RequestParam parameters: MultiValueMap<String, String>): String {
        return "OK"
    }

    @RequestMapping("/sosialhjelp/mock-alt-api/internal/isReady")
    fun isReady(@RequestParam parameters: MultiValueMap<String, String>): String {
        return "OK"
    }
}
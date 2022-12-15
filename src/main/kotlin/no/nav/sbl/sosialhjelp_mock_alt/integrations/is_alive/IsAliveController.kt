package no.nav.sbl.sosialhjelp_mock_alt.integrations.is_alive

import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IsAliveController {
  @GetMapping("/internal/isAlive")
  fun isAlive(@RequestParam parameters: MultiValueMap<String, String>): String {
    return "OK"
  }

  @GetMapping("/internal/isReady")
  fun isReady(@RequestParam parameters: MultiValueMap<String, String>): String {
    return "OK"
  }
}

package no.nav.sbl.sosialhjelp_mock_alt.integrations.fssproxy

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class FssProxyController {

    @RequestMapping("/fss-proxy/ping", method = [RequestMethod.OPTIONS])
    fun ping(): String {
        log.info("Ping \"fss-proxy\"")
        return "OK"
    }

    companion object {
        private val log by logger()
    }
}

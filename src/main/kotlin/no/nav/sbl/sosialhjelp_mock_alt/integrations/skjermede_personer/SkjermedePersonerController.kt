package no.nav.sbl.sosialhjelp_mock_alt.integrations.skjermede_personer

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SkjermedePersonerController() {
    companion object {
        private val log by logger()
    }

    @PostMapping("/skjermede-personer/skjermet")
    fun erPersonSkjermet(): Boolean {
        log.info("Er person skjemet? false")
        return false
    }
}

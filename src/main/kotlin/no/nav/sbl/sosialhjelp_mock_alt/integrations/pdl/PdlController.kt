package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl

import no.nav.sbl.sosialhjelp_mock_alt.integrations.norg.model.NavEnhet
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PdlController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/pdl_endpoint_url")
    fun dummyEndpoint(): String {
        log.info("Henter pdl_endpoint_url")
        return "OK"
    }
}
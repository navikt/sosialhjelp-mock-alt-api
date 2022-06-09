package no.nav.sbl.sosialhjelp_mock_alt.integrations.skjermede_personer

import no.nav.sbl.sosialhjelp_mock_alt.datastore.skjermedepersoner.SkjermedePersonerService
import no.nav.sbl.sosialhjelp_mock_alt.integrations.skjermede_personer.model.SkjermedePersonerRequest
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SkjermedePersonerController(private val skjermedePersonerService: SkjermedePersonerService) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/skjermede-personer/skjermet")
    fun erPersonSkjermet(@RequestBody body: String): Boolean {
        val request = objectMapper.readValue(body, SkjermedePersonerRequest::class.java)
        val status = skjermedePersonerService.getStatus(request.personIdent)
        log.info("Er person ${request.personIdent} skjemet? = $status")
        return status
    }
}

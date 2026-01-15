package no.nav.sbl.sosialhjelp.mock.alt.integrations.skjermedepersoner

import no.nav.sbl.sosialhjelp.mock.alt.datastore.skjermedepersoner.SkjermedePersonerService
import no.nav.sbl.sosialhjelp.mock.alt.integrations.skjermedepersoner.model.SkjermedePersonerRequest
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SkjermedePersonerController(
    private val skjermedePersonerService: SkjermedePersonerService,
) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/skjermede-personer/skjermet")
    fun erPersonSkjermet(
        @RequestBody body: String,
    ): Boolean {
        val request = objectMapper.readValue(body, SkjermedePersonerRequest::class.java)
        val status = skjermedePersonerService.getStatus(request.personIdent)
        log.info("Er person ${request.personIdent} skjemet? = $status")
        return status
    }
}

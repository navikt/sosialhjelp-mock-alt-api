package no.nav.sbl.sosialhjelp_mock_alt.integrations.enhetsregisteret

import no.nav.sbl.sosialhjelp_mock_alt.datastore.enhetsregisteret.EnhetsregisteretService
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class EnhetsregisteretController(
    private val enhetsregisteretService: EnhetsregisteretService
) {

    @GetMapping("/enhetsregisteret/api/enheter/{orgnr}")
    fun getNokkelinfo(
        @PathVariable orgnr: String,
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<String> {
        val enhet = enhetsregisteretService.getEnhet(orgnr)
        log.info("Henter enhet fra enhetsregisteret: $enhet")
        return ResponseEntity.ok(enhet)
    }

    companion object {
        private val log by logger()
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.feil

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.Feilsituasjon
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FeilController(val feilService: FeilService) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/feil/edit_feil")
    fun editFeil(@RequestBody feilsituasjon: Feilsituasjon ) {
        log.warn(objectMapper.writeValueAsString(feilsituasjon))
        feilService.legtilFeil(feilsituasjon)
    }

    @GetMapping("feil/hent_feil")
    fun hentFeil(@RequestParam ident: String): ResponseEntity<Feilsituasjon> {
        val feil = feilService.hentFeil(ident)
        if(feil != null) {
            return ResponseEntity.ok(feil)
        } else {
            return ResponseEntity.noContent().build()
        }
    }
}

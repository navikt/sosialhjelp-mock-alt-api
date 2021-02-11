package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.feil

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.Feilsituasjon
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

    @PostMapping("/feil")
    fun editFeil(@RequestBody feilsituasjoner: FeilsituasjonerFrontend ) {
        feilService.setFeilForFnr(feilsituasjoner.fnr, feilsituasjoner.feilsituasjoner)
    }

    @GetMapping("/feil")
    fun hentFeil(@RequestParam ident: String): ResponseEntity<FeilsituasjonerFrontend> {
        return ResponseEntity.ok(FeilsituasjonerFrontend(ident, feilService.hentFeil(ident)))
    }
}

data class FeilsituasjonerFrontend(
        val fnr: String,
        val feilsituasjoner: List<Feilsituasjon>
)

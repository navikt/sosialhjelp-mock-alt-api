package no.nav.sbl.sosialhjelp_mock_alt.integrations.kontonummer

import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontonummerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.model.KontonummerDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class KontonummerController(
    private val kontonummerService: KontonummerService
) {

    @GetMapping("/oppslag-api/kontonummer")
    fun kontonummerEndpoint(
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<KontonummerDto> {
        val ident = hentFnrFraToken(headers)
        val dto = kontonummerService.getKontonummer(ident)
        log.info("Henter kontonummer: ${objectMapper.writeValueAsString(dto)}")
        return ResponseEntity.ok().body(dto)
    }

    companion object {
        private val log by logger()
    }
}

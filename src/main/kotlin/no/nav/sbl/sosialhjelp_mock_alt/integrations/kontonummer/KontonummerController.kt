package no.nav.sbl.sosialhjelp_mock_alt.integrations.kontonummer

import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontonummerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontoregisterService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.model.KontoDto
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
    private val kontonummerService: KontonummerService,
    private val kontoregisterService: KontoregisterService
) {

    @GetMapping("/oppslag-api/kontonummer")
    fun kontonummerEndpoint(
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<KontonummerDto> {
        val ident = hentFnrFraToken(headers)
        val dto = kontonummerService.getKontonummer(ident)
        log.info("Henter kontonummer: ${objectMapper.writeValueAsString(dto)}")
        return dto?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/api/borger/v1/hent-aktiv-konto")
    fun hentKontoNummerFraKontoRegister(
        @RequestHeader headers: HttpHeaders) : ResponseEntity<KontoDto>{
        val ident = hentFnrFraToken(headers)
        val responseDto = kontoregisterService.getKonto(ident)
        log.info("Henter konto: ${objectMapper.writeValueAsString(responseDto)}")

        return responseDto?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

    }

    companion object {
        private val log by logger()
    }
}

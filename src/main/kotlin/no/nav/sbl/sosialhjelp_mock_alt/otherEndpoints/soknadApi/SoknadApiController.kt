package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class SoknadApiController(
    private val pdlService: PdlService,
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("soknad-api/dialog/sistInnsendteSoknad")
    @ResponseBody
    fun soknadStatus(@RequestHeader headers: HttpHeaders): ResponseEntity<SoknadStatusDto> {
        val ident = hentFnrFraToken(headers)
        log.info("Henter soknadsstatus for brukerId $ident")
        return try {
            val personalia = pdlService.getPersonalia(ident)
            val status = if (personalia.navn.mellomnavn == "IngenSoknader") {
                null
            } else {
                SoknadStatusDto(ident, "Hamar kommune", LocalDateTime.now())
            }
            ResponseEntity.ok(status)
        } catch (e: MockAltException) {
            log.info("Feil ved henting av brukers soknadsstatus: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("soknad-api/soknadoversikt/soknader")
    @ResponseBody
    fun soknadoversikt(@RequestHeader headers: HttpHeaders): ResponseEntity<List<SaksListeDto>> {
        // dummy endepunkt som returnerer 200 OK med en tom liste.
        return ResponseEntity.ok(emptyList())
    }
}

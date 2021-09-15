package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Autowired
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
    @Autowired val pdlService: PdlService,
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("soknad-api/dialog/sistInnsendteSoknad")
    @ResponseBody
    fun soknadStatus(@RequestHeader headers: HttpHeaders): ResponseEntity<SoknadStatus> {
        val ident = hentFnrFraToken(headers)
        log.info("Henter soknadsstatus for brukerId $ident")
        return try {
            val personalia = pdlService.getPersonalia(ident)
            val status = if (personalia.navn.mellomnavn == "IngenSoknader") {
                null
            } else {
                SoknadStatus(ident, "0315", LocalDateTime.now())
            }
            ResponseEntity.ok(status)
        } catch (e: MockAltException) {
            log.info("Feil ved henting av brukers soknadsstatus: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.dialogApi

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DialogApiController(
    private val pdlService: PdlService,
) {
    companion object {
        private val log by logger()
    }

    @PostMapping("dialog-api/status")
    @ResponseBody
    fun soknadStatus(@RequestHeader headers: HttpHeaders, @RequestBody body: DialogStatusRequest): ResponseEntity<DialogStatus> {
        val ident = hentFnrFraToken(headers)
        if (ident != body.ident) log.warn("Token ident $ident matcher ikke request ident ${body.ident}.")
        log.info("Henter soknadsstatus for brukerId $ident")
        return try {
            val personalia = pdlService.getPersonalia(ident)
            val status = if (personalia.navn.mellomnavn == "IngenSoknader") {
                null
            } else {
                DialogStatus(ident, true, 1)
            }
            ResponseEntity.ok(status)
        } catch (e: MockAltException) {
            log.info("Feil ved henting av brukers soknadsstatus: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}

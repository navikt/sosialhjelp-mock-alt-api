package no.nav.sbl.sosialhjelp.mock.alt.integrations.fiks

import no.nav.sbl.sosialhjelp.mock.alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp.mock.alt.utils.hentFnrFraHeadersNoDefault
import no.nav.sbl.sosialhjelp.mock.alt.utils.hentFnrFraToken
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class BulkDokumenterRequest(
    val dokumenter: List<BulkDokumentRequest>,
)

data class BulkDokumentRequest(
    val digisosId: String,
    val dokumentlagerId: String,
)

@RestController
class FiksBulkDokumentController(
    private val soknadService: SoknadService,
    private val dokumentlagerService: DokumentlagerService,
    private val feilService: FeilService,
) {
    @PostMapping("/fiks/digisos/api/v1/soknader/dokumenter")
    fun hentDokumenter(
        @RequestBody request: BulkDokumenterRequest,
        @RequestHeader headers: HttpHeaders,
    ): ResponseEntity<ByteArray> {
        val fnr = hentFnrFraHeadersNoDefault(headers) ?: hentFnrFraToken(headers)
        feilService.eventueltLagFeil(fnr, "FixController", "hentDokument")

        val dokumenter =
            request.dokumenter.map { dokument ->
                if (!dokument.digisosId.matches(SAFE_ID) || !dokument.dokumentlagerId.matches(SAFE_ID)) {
                    return ResponseEntity.badRequest().build()
                }
                val sak = soknadService.hentSoknad(dokument.digisosId)
                if (sak == null || sak.sokerFnr != fnr || sak.digisosSoker?.metadata != dokument.dokumentlagerId) {
                    return ResponseEntity.notFound().build()
                }
                val json =
                    dokumentlagerService.hentDokument(dokument.digisosId, dokument.dokumentlagerId)
                        ?: return ResponseEntity.notFound().build()
                dokument to json
            }

        val boundary = UUID.randomUUID().toString()
        val body =
            buildString {
                dokumenter.forEach { (dokument, json) ->
                    append("--$boundary\r\n")
                    append("Content-Disposition: form-data; name=\"${dokument.digisosId}_${dokument.dokumentlagerId}\"\r\n")
                    append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                    append(json)
                    append("\r\n")
                }
                append("--$boundary--\r\n")
            }
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType("multipart/mixed; boundary=$boundary"))
            .body(body.toByteArray(Charsets.UTF_8))
    }

    companion object {
        private val SAFE_ID = Regex("[A-Za-z0-9-]+")
    }
}

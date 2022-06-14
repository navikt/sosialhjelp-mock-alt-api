package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks.mellomlagring

import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.InputStream

@RestController
class FiksMellomlagringController(
    private val feilService: FeilService,
    private val mellomlagringService: MellomlagringService
) {

    @GetMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}")
    fun getAllMellomlagredeVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String
    ): ResponseEntity<Any> {
        feilService.eventueltLagFeil(headers, "FiksMellomlagringController", "getAllMellomlagredeVedlegg")
        val dto = mellomlagringService.getAll(navEksternRefId)
        return dto?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.badRequest()
                .body(
                    ErrorMessage(
                        error = null,
                        errorCode = null,
                        errorId = null,
                        errorJson = null,
                        message = "Fant ingen data i basen knytter til angitt id'en $navEksternRefId",
                        originalPath = null,
                        path = null,
                        status = 400,
                        timestamp = null
                    )
                )
    }

    @GetMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}")
    fun getMellomlagretVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
        @PathVariable digisosDokumentId: String,
    ): ResponseEntity<ByteArray> {
        feilService.eventueltLagFeil(headers, "FiksMellomlagringController", "getMellomlagretVedlegg")
        val bytes = mellomlagringService.get(navEksternRefId, digisosDokumentId)
        return ResponseEntity.ok(bytes)
    }

    @DeleteMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}")
    fun deleteAllMellomlagredeVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
    ): ResponseEntity<String> {
        feilService.eventueltLagFeil(headers, "FiksMellomlagringController", "deleteAllMellomlagredeVedlegg")
        mellomlagringService.deleteAll(navEksternRefId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}")
    fun deleteMellomlagretVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
        @PathVariable digisosDokumentId: String,
    ): ResponseEntity<String> {
        feilService.eventueltLagFeil(headers, "FiksMellomlagringController", "deleteMellomlagretVedlegg")
        mellomlagringService.delete(navEksternRefId, digisosDokumentId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}")
    fun postMellomlagretVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
        @RequestParam body: LinkedMultiValueMap<String, Any>,
        request: StandardMultipartHttpServletRequest
    ): ResponseEntity<String> {
        feilService.eventueltLagFeil(headers, "FiksMellomlagringController", "postMellomlagretVedlegg")
        // fisk ut filnavn, bytes og mimetype fra request/multipart
        val filMetadata = objectMapper.readValue(request.parameterMap["metadata"]!![0], FilMetadata::class.java)
        val file = request.fileMap[filMetadata.filnavn] ?: throw RuntimeException("Fant ikke fil for mellomlagring")

        mellomlagringService.post(
            navEksternRefId = navEksternRefId,
            filnavn = filMetadata.filnavn,
            bytes = file.bytes,
            mimeType = filMetadata.mimetype
        )
        return ResponseEntity.ok().build()
    }

    companion object {
        private val log by logger()
    }

    data class FilForOpplasting(
        val filnavn: String,
        val metadata: FilMetadata,
        val data: InputStream
    )

    data class FilMetadata(
        val filnavn: String,
        val mimetype: String,
        val storrelse: Long
    )
}

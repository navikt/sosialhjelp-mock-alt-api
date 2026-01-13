package no.nav.sbl.sosialhjelp.mock.alt.integrations.fiks.mellomlagring

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp.mock.alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

@RestController
class FiksMellomlagringController(
    private val feilService: FeilService,
    private val mellomlagringService: MellomlagringService,
) {
    @GetMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}")
    fun getAllMellomlagredeVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
    ): ResponseEntity<Any> {
        feilService.eventueltLagFeil(
            headers,
            "FiksMellomlagringController",
            "getAllMellomlagredeVedlegg",
        )
        val dto = mellomlagringService.getAll(navEksternRefId)
        return dto?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.ok(MellomlagringDto(navEksternRefId, emptyList()))
        //        ?: ResponseEntity.of(
        //            Optional.of(

        //            .body(
        //                ErrorMessage(
        //                    error = null,
        //                    errorCode = null,
        //                    errorId = null,
        //                    errorJson = null,
        //                    message = "Fant ingen data i basen knytter til angitt id'en
        // $navEksternRefId",
        //                    originalPath = null,
        //                    path = null,
        //                    status = 400,
        //                    timestamp = null))
        //            )
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
        feilService.eventueltLagFeil(
            headers,
            "FiksMellomlagringController",
            "deleteAllMellomlagredeVedlegg",
        )
        mellomlagringService.deleteAll(navEksternRefId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}")
    fun deleteMellomlagretVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
        @PathVariable digisosDokumentId: String,
    ): ResponseEntity<String> {
        feilService.eventueltLagFeil(
            headers,
            "FiksMellomlagringController",
            "deleteMellomlagretVedlegg",
        )
        mellomlagringService.delete(navEksternRefId, digisosDokumentId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/fiks/digisos/api/v1/mellomlagring/{navEksternRefId}")
    fun postMellomlagretVedlegg(
        @RequestHeader headers: HttpHeaders,
        @PathVariable navEksternRefId: String,
        @RequestParam("files") files: List<MultipartFile>,
        request: MultipartHttpServletRequest,
    ): ResponseEntity<Any> {
        feilService.eventueltLagFeil(headers, "FiksMellomlagringController", "postMellomlagretVedlegg")

        //        fisk ut filnavn, bytes og mimetype fra request/multipart
        request.parameterMap["metadata"]!!
            .map { objectMapper.readValue<FilMetadata>(it) }
            .forEach { filMetadata ->
                files
                    .find { it.originalFilename == filMetadata.filnavn }
                    ?.also { file ->
                        mellomlagringService.lagreFil(
                            navEksternRefId = navEksternRefId,
                            filnavn = filMetadata.filnavn,
                            bytes = file.bytes,
                            mimeType = filMetadata.mimetype,
                        )
                    } ?: error("Fant ikke fil for Metadata")
            }

        return mellomlagringService
            .getAll(navEksternRefId)
            ?.mellomlagringMetadataList
            ?.let { MellomlagringDto(navEksternRefId, mellomlagringMetadataList = it) }
            ?.let { ResponseEntity.ok(it) } ?: createError(navEksternRefId)
    }
}

private fun createError(navEksternRefId: String): ResponseEntity<Any> =
    ResponseEntity
        .badRequest()
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
                timestamp = null,
            ),
        )

data class FilMetadata(
    val filnavn: String,
    val mimetype: String,
    val storrelse: Long,
)

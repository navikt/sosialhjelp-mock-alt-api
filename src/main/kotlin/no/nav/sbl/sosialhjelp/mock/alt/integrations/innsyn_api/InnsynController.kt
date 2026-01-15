package no.nav.sbl.sosialhjelp.mock.alt.integrations.innsynapi

import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.model.VedleggMetadata
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.FAST_FNR
import no.nav.sbl.sosialhjelp.mock.alt.utils.hentFnrFraCookieNoDefault
import no.nav.sbl.sosialhjelp.mock.alt.utils.hentFnrFraHeadersNoDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
class InnsynController(
    private val soknadService: SoknadService,
    private val dokumentlagerService: DokumentlagerService,
) {
    @PostMapping("/innsyn-api/api/v1/digisosapi/oppdaterDigisosSak")
    fun oppdaterSoknad(
        @RequestParam(required = false) fiksDigisosId: String?,
        @RequestParam(required = false) fnr: String?,
        @RequestParam(required = false) isPapirSoknad: String?,
        @RequestBody body: String,
        @RequestHeader headers: HttpHeaders,
        @CookieValue(name = "localhost-idtoken") cookie: String?,
    ): ResponseEntity<String> {
        var id = fiksDigisosId
        if (id == null || id == "001" || id == "002" || id == "003") {
            id = UUID.randomUUID().toString()
        }
        val digisosApiWrapper = objectMapper.readValue(body, DigisosApiWrapper::class.java)

        val faktiskFnr = hentFnrFraInputOrTokenOrCookieOrDefault(fnr, headers, cookie)
        soknadService.oppdaterDigisosSak(
            kommuneNr = "0301",
            fiksOrgId = "11415cd1-e26d-499a-8421-751457dfcbd5",
            fnr = faktiskFnr,
            fiksDigisosIdInput = id,
            digisosApiWrapper = digisosApiWrapper,
            isPapirSoknad = isPapirSoknad.toBoolean(),
        )
        return ResponseEntity.ok("{\"fiksDigisosId\":\"$id\"}")
    }

    @PostMapping(
        "/innsyn-api/api/v1/digisosapi/{fiksDigisosId}/filOpplasting",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun filOpplasting(
        @PathVariable fiksDigisosId: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<String> {
        val vedleggMetadata = VedleggMetadata(file.originalFilename, file.contentType, file.size)
        val dokumentlagerId = soknadService.lastOppFil(fiksDigisosId, vedleggMetadata, file = file)
        return ResponseEntity.ok(dokumentlagerId)
    }

    @GetMapping("/innsyn-api/api/v1/digisosapi/{digisosId}/innsynsfil")
    fun hentInnsynsfil(
        @PathVariable digisosId: String,
    ): ResponseEntity<ByteArray> {
        val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.noContent().build()
        val innsynsfil =
            dokumentlagerService.hentDokument(digisosId, soknad.digisosSoker!!.metadata)
                ?: return ResponseEntity.noContent().build()
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(innsynsfil.toByteArray())
    }

    private fun hentFnrFraInputOrTokenOrCookieOrDefault(
        fnrInput: String?,
        headers: HttpHeaders,
        cookie: String?,
    ): String =
        fnrInput
            ?: hentFnrFraHeadersNoDefault(headers)
            ?: hentFnrFraCookieNoDefault(cookie)
            ?: FAST_FNR
}

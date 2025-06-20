package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import no.nav.sbl.sosialhjelp_mock_alt.MissingRequiredPartException
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.Dokumentlager
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeadersNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraTokenNoDefault
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest

@RestController
@RequestMapping("/digisos/klage/api/v1/{digisosId}/{kommunenummer}/{navEksternRefId}")
class KlageController(
    private val klageService: KlageService,
    private val mellomlagringService: MellomlagringService,
    private val soknadService: SoknadService,
    private val dokumentlager: Dokumentlager,
) {

  @PostMapping("/{klageId}/vedlegg")
  fun lastOppVedlegg(
      @PathVariable digisosId: UUID,
      @PathVariable kommunenummer: String,
      @PathVariable navEksternRefId: UUID,
      @PathVariable klageId: UUID,
      @RequestParam body: LinkedMultiValueMap<String, Any>,
      request: StandardMultipartHttpServletRequest,
  ) {}

  @PostMapping("/{klageId}")
  fun sendKlage(
      @PathVariable digisosId: UUID,
      @PathVariable kommunenummer: String,
      @PathVariable navEksternRefId: UUID,
      @PathVariable klageId: UUID,
      request: StandardMultipartHttpServletRequest,
  ) {
    val personId =
        hentFnrFraHeadersNoDefault(request.requestHeaders)
            ?: hentFnrFraToken(request.requestHeaders)

    val klagePdf =
        request.fileMap["klagePdf"]
            ?: throw MissingRequiredPartException("Fil (klagePdf) mangler i request")

    val klagePdfDokumentId =
        klagePdf.originalFilename?.let { soknadService.leggInnIDokumentlager(it, klagePdf.bytes) }
            ?: throw MissingRequiredPartException("Filnavn (klage.pdf) mangler i request")

    val klageJsonDokumentId =
        request.parameterMap["klageJson"]?.let { dokumentlager.save(data = it[0]) }
            ?: throw MissingRequiredPartException("Fil (klageJson) mangler i request")

    val vedleggJsonDokumentId =
        request.parameterMap["vedleggJson"]?.let { dokumentlager.save(data = it[0]) }
            ?: throw MissingRequiredPartException("Fil (vedleggJson) mangler i request")

    val mellomlagringDto = mellomlagringService.getAll(navEksternRefId.toString())

    val digisosVedleggInfo =
        mellomlagringDto?.mellomlagringMetadataList?.map { dokInfo ->
          soknadService.leggInnIDokumentlager(
              vedleggsId = dokInfo.filId,
              filnavn = dokInfo.filnavn,
              bytes = mellomlagringService.get(navEksternRefId.toString(), dokInfo.filId))

          DigisosVedlegg(
              filnavn = dokInfo.filnavn,
              dokumentlagerDokumentId = UUID.fromString(dokInfo.filId),
              storrelse = dokInfo.storrelse)
        }

    mellomlagringService.deleteAll(navEksternRefId.toString())

    klageService.leggTilKlage(
        digisosId = digisosId,
        kommunenummer = kommunenummer,
        navEksternRefId = navEksternRefId,
        klageId = klageId,
        personId = personId,
        klageJsonDokumentId = klageJsonDokumentId,
        vedleggJsonDokumentId = vedleggJsonDokumentId,
        vedleggPdfDokumentId = UUID.fromString(klagePdfDokumentId),
        vedleggPdfStorrelse = klagePdf.size,
        vedleggSpec = digisosVedleggInfo)
  }

  @PostMapping("/{klageId}/trekk")
  fun trekkKlage(
      @PathVariable digisosId: UUID,
      @PathVariable kommunenummer: String,
      @PathVariable navEksternRefId: UUID,
      @PathVariable klageId: UUID,
  ) {
    throw NotImplementedError("Ikke en del av første versjon av Klage")
  }

  @GetMapping("/klager")
  fun hentKlager(
      @RequestHeader headers: HttpHeaders,
  ): List<DigisosKlagerMetadata> {
    val personId = hentFnrFraTokenNoDefault(headers) ?: hentFnrFraToken(headers)
    return klageService.hentAlleKlagerForPerson(personId)
  }
}

data class JsonKlage(
    val id: UUID,
    val fiksDigisosId: UUID,
    val klageTekst: String,
    val vedtaksIds: List<String>,
)

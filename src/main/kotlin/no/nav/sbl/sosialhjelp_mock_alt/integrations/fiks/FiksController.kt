package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks

import com.fasterxml.jackson.core.type.TypeReference
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonAvsender
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.DokumentKrypteringsService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.KommuneInfoService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.JsonTilleggsinformasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.SakWrapper
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model.VedleggMetadata
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.fastFnr
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigOrganisasjonsnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraBody
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeaders
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeadersNoDefault
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest

@RestController
class FiksController(
    private val soknadService: SoknadService,
    private val dokumentKrypteringsService: DokumentKrypteringsService,
    private val feilService: FeilService,
    private val pdlService: PdlService,
    private val kommuneInfoService: KommuneInfoService,
    private val mellomlagringService: MellomlagringService,
) {
  companion object {
    private val log by logger()
  }

  //    ======== Innsyn =========
  @GetMapping("/fiks/digisos/api/v1/soknader/soknader")
  fun listSoknaderInnsyn(@RequestHeader headers: HttpHeaders): ResponseEntity<String> {
    val fnr = hentFnrFraHeadersNoDefault(headers) ?: hentFnrFraToken(headers)
    feilService.eventueltLagFeil(fnr, "FixController", "hentSoknad")
    val soknadsListe = soknadService.listSoknader(fnr)
    return ResponseEntity.ok(objectMapper.writeValueAsString(soknadsListe))
  }

  @GetMapping("/fiks/digisos/api/v1/soknader/{digisosId}")
  fun hentSoknadInnsyn(@PathVariable digisosId: String): ResponseEntity<DigisosSak> {
    val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
    feilService.eventueltLagFeil(soknad.sokerFnr, "FixController", "hentSoknad")
    return ResponseEntity.ok(soknad)
  }

  @GetMapping("/fiks/dokumentlager/dokumentlager/nedlasting/niva4/{dokumentlagerId}")
  fun hentDokumentFraLagerInnsynNiva4(
      @PathVariable dokumentlagerId: String,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<ByteArray> {
    feilService.eventueltLagFeil(headers, "FixController", "hentSoknad")
    val fil = soknadService.hentFil(dokumentlagerId)
    if (fil != null) {
      val mediaType =
          if (fil.filnavn.lowercase().endsWith(".png")) MediaType.IMAGE_PNG
          else if (fil.filnavn.lowercase().endsWith(".jpeg") ||
              fil.filnavn.lowercase().endsWith(".jpg"))
              MediaType.IMAGE_JPEG
          else MediaType.APPLICATION_PDF
      return ResponseEntity.ok()
          .contentType(mediaType)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fil.filnavn + "\"")
          .body(fil.bytes)
    }
    val dokumentString =
        soknadService.hentDokument(null, dokumentlagerId)
            ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(dokumentString.toByteArray())
  }

  @GetMapping("/fiks/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
  fun hentDokumentFraLagerInnsyn(
      @PathVariable digisosId: String,
      @PathVariable dokumentlagerId: String,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "hentDokument")
    val dokumentString =
        soknadService.hentDokument(digisosId, dokumentlagerId)
            ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(dokumentString)
  }

  @PostMapping("/fiks/digisos/api/v1/{fiksOrgId}/{digisosId}/filer")
  fun lastOppFilerInnsyn(
      @PathVariable digisosId: String,
      @PathVariable(required = false) fiksOrgId: String?,
      @RequestParam body: LinkedMultiValueMap<String, Any>,
      @RequestHeader headers: HttpHeaders,
      request: StandardMultipartHttpServletRequest
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "lastOpp")
    return lastOppFiler("", digisosId, "", body, headers, request)
  }

  @PostMapping("/fiks/digisos/api/v1/{fiksOrgId}/{fiksDigisosId}") // tar også /ny
  fun oppdaterSoknadInnsyn(
      @PathVariable fiksOrgId: String,
      @PathVariable(required = false) fiksDigisosId: String?,
      @RequestBody(required = false) body: String?,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<String> {
    var id = fiksDigisosId
    val fnr = hentFnrFraHeaders(headers)
    feilService.eventueltLagFeil(fnr, "FixController", "lastOppSoknad")
    val kommuneNr =
        try {
          pdlService.getPersonalia(fnr).bostedsadresse.kommunenummer
        } catch (e: MockAltException) {
          "0301"
        }
    return if (id == null || id.lowercase().contentEquals("ny")) {
      id = UUID.randomUUID().toString()
      soknadService.opprettDigisosSak(fiksOrgId, kommuneNr, fnr, id)
      ResponseEntity.ok("$id")
    } else {
      val digisosApiWrapper = objectMapper.readValue(body, DigisosApiWrapper::class.java)
      soknadService.oppdaterDigisosSak(
          kommuneNr = kommuneNr,
          fiksOrgId = fiksOrgId,
          fnr = fnr,
          fiksDigisosIdInput = id,
          digisosApiWrapper = digisosApiWrapper)
      ResponseEntity.ok("{\"fiksDigisosId\":\"$id\"}")
    }
  }

  // Soknad
  @PostMapping("/fiks/digisos/api/v2/soknader/{kommuneNr}/{fiksDigisosId}")
  fun lastOppSoknad(
      @PathVariable kommuneNr: String,
      @PathVariable(required = false) fiksDigisosId: String?,
      request: StandardMultipartHttpServletRequest
  ): ResponseEntity<String> {

    val id = fiksDigisosId ?: UUID.randomUUID().toString()
    val digisosApiWrapper = DigisosApiWrapper(SakWrapper(JsonDigisosSoker()), "")
    digisosApiWrapper.sak.soker.hendelser.add(
        JsonSoknadsStatus()
            .withHendelsestidspunkt(
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
            .withType(JsonHendelse.Type.SOKNADS_STATUS)
            .withStatus(JsonSoknadsStatus.Status.MOTTATT))
    digisosApiWrapper.sak.soker.avsender =
        JsonAvsender().withSystemnavn("mock-alt").withSystemversjon("1.0-MOCKVERSJON")

    val soknadJson =
        objectMapper.readValue(request.parameterMap["soknadJson"]!![0], JsonSoknad::class.java)
    val vedleggJson =
        objectMapper.readValue(
            request.parameterMap["vedleggJson"]!![0], JsonVedleggSpesifikasjon::class.java)
    val fnr = soknadJson.data.personalia.personIdentifikator.verdi
    feilService.eventueltLagFeil(fnr, "FixController", "lastOppSoknad")
    val tilleggsinformasjonJson =
        objectMapper.readValue(
            request.parameterMap["tilleggsinformasjonJson"]!![0],
            JsonTilleggsinformasjon::class.java)
    val enhetsnummer = tilleggsinformasjonJson.enhetsnummer

    val dokumenter = mutableListOf<DokumentInfo>()
    request.fileMap.forEach { (filnavn, fil) ->
      val dokumentLagerId = soknadService.leggInnIDokumentlager(filnavn, fil.bytes)
      val dokumentInfo =
          DokumentInfo(
              filnavn = filnavn,
              dokumentlagerDokumentId = dokumentLagerId,
              storrelse = fil.size,
          )
      dokumenter.add(dokumentInfo)
    }

    // hent ut mellomlagrede vedlegg
    val mellomlagringDto = mellomlagringService.getAll(navEksternRefId = id)
    mellomlagringDto?.mellomlagringMetadataList?.forEach {
      val dokumentlagerId =
          soknadService.leggInnIDokumentlager(
              filnavn = it.filnavn,
              bytes = mellomlagringService.get(navEksternRefId = id, digisosDokumentId = it.filId))
      val dokumentInfo =
          DokumentInfo(
              filnavn = it.filnavn,
              dokumentlagerDokumentId = dokumentlagerId,
              storrelse = it.storrelse,
          )
      dokumenter.add(dokumentInfo)
    }
    // fjern mellomlagrede vedlegg
    mellomlagringService.deleteAll(navEksternRefId = id)

    soknadService.oppdaterDigisosSak(
        kommuneNr = kommuneNr,
        fiksOrgId = null,
        fnr = fnr!!,
        fiksDigisosIdInput = id,
        enhetsnummer = enhetsnummer,
        digisosApiWrapper = digisosApiWrapper,
        jsonSoknad = soknadJson,
        jsonVedlegg = vedleggJson,
        dokumenter = dokumenter,
        soknadDokument = dokumenter.firstOrNull { it.filnavn.lowercase() == "soknad.pdf" })
    return ResponseEntity.ok(id)
  }

  //    ======== Modia =========
  @PostMapping("/fiks/digisos/api/v1/nav/soknader/soknader")
  fun listSoknaderModia(
      @RequestBody body: String,
      @RequestParam(name = "sporingsId") sporingsId: String,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "hentSoknad")
    val soknadsListe = soknadService.listSoknader(hentFnrFraBody(body))
    return ResponseEntity.ok(objectMapper.writeValueAsString(soknadsListe))
  }

  @GetMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}")
  fun hentSoknadModia(
      @PathVariable digisosId: String,
      @RequestParam(name = "sporingsId") sporingsId: String,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<DigisosSak> {
    feilService.eventueltLagFeil(headers, "FixController", "hentSoknad")
    val soknad = soknadService.hentSoknad(digisosId) ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(soknad)
  }

  @GetMapping("/fiks/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}")
  fun hentDokumentFraLagerModia(
      @PathVariable digisosId: String,
      @PathVariable dokumentlagerId: String,
      @RequestParam(name = "sporingsId") sporingsId: String,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "hentDokument")
    val dokumentString =
        soknadService.hentDokument(digisosId, dokumentlagerId)
            ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(dokumentString)
  }

  //    ======= KommuneInfo =======
  @GetMapping("/fiks/digisos/api/v1/nav/kommuner/{kommunenummer}")
  fun hentKommuneInfo(
      @PathVariable kommunenummer: String,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "kommuneinfo")
    val kommuneInfo = kommuneInfoService.getKommuneInfo(kommunenummer)
    log.info("Henter kommuneinfo: $kommuneInfo")
    return ResponseEntity.ok(objectMapper.writeValueAsString(kommuneInfo))
  }

  @GetMapping("/fiks/digisos/api/v1/nav/kommuner")
  fun hentKommuneInfoListe(@RequestHeader headers: HttpHeaders): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "kommuneinfo")
    val kommuneInfoList = kommuneInfoService.hentAlleKommuner()
    log.info("Henter kommuneinfo: ${kommuneInfoList.size}")
    return ResponseEntity.ok(objectMapper.writeValueAsString(kommuneInfoList))
  }

  //    ======== public-key dokumentlager ========
  @GetMapping("/fiks/digisos/api/v1/dokumentlager-public-key")
  fun hentDokumentLagerNokkel(): ResponseEntity<ByteArray> {
    log.info("Henter dokumentlager public key:")
    return ResponseEntity.ok(dokumentKrypteringsService.publicCertificateBytes)
  }

  //    ======== Last opp filer ========
  @PostMapping("/fiks/digisos/api/v1/soknader/{kommunenummer}/{digisosId}/{navEksternRefId}")
  fun lastOppFiler(
      @PathVariable kommunenummer: String,
      @PathVariable digisosId: String,
      @PathVariable navEksternRefId: String,
      @RequestParam body: LinkedMultiValueMap<String, Any>,
      @RequestHeader headers: HttpHeaders,
      request: StandardMultipartHttpServletRequest,
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "lastOpp")
    log.info(
        "Laster opp filer for kommune: $kommunenummer digisosId: $digisosId navEksternRefId: $navEksternRefId")
    val vedleggsInfoText: String = body["vedlegg.json"].toString()
    val vedleggsJson =
        objectMapper
            .readValue(
                vedleggsInfoText, object : TypeReference<List<JsonVedleggSpesifikasjon>>() {})
            .first()
    body.keys.forEach { key ->
      if (key.startsWith("vedleggSpesifikasjon")) {
        val json = body[key].toString()
        val vedleggMetadata =
            objectMapper.readValue(json, object : TypeReference<List<VedleggMetadata>>() {}).first()
        val file = request.fileMap.values.find { it.originalFilename == vedleggMetadata.filnavn }
        soknadService.lastOppFil(
            fiksDigisosId = digisosId,
            vedleggMetadata = vedleggMetadata,
            vedleggsJson = vedleggsJson,
            file = file,
        )
      }
    }
    return ResponseEntity.ok("OK")
  }

  @PostMapping("/{fiksDigisosId}/filOpplasting", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun filOpplasting(
      @PathVariable fiksDigisosId: String,
      @RequestParam("file") file: MultipartFile,
      @RequestHeader headers: HttpHeaders,
  ): ResponseEntity<String> {
    feilService.eventueltLagFeil(headers, "FixController", "lastOpp")
    log.info("Laster opp fil for fiksDigisosId: $fiksDigisosId")
    val vedleggMetadata = VedleggMetadata(file.originalFilename, file.contentType, file.size)
    val dokumentlagerId =
        soknadService.lastOppFil(
            fiksDigisosId = fiksDigisosId, vedleggMetadata = vedleggMetadata, file = file)

    return ResponseEntity.ok(dokumentlagerId)
  }

  //    ======== Util =========
  @GetMapping("/fiks/ping") fun ping(): String = "pong"

  @GetMapping("/fiks/tilfeldig/orgnummer")
  fun getTilfeldigOrgnummer(): String = genererTilfeldigOrganisasjonsnummer()

  @GetMapping("/fiks/tilfeldig/fnr") fun getTilfeldigFnr(): String = genererTilfeldigPersonnummer()

  @GetMapping("/fiks/fast/fnr") fun getFastFnr(): String = fastFnr
}

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendSoknad
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Denne og FrontendUserControllerV2 er ment å erstatte FrontendController. */
@RestController
@RequestMapping("/mock-alt/v2/soknader")
class FrontendSoknadControllerV2(
    private val pdlService: PdlService,
    private val soknadService: SoknadService,
    private val soknadArchiveService: FrontendArchiveService,
) {
  @GetMapping("{fiksDigisosId}/soknadZip", produces = ["application/zip"])
  fun getSoknadZipV2(@PathVariable fiksDigisosId: String): ResponseEntity<ByteArray> {
    val soknad =
        soknadService.hentSoknad(fiksDigisosId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    val soknadZip = soknadArchiveService.makeSoknadZip(soknad)

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=soknad_$fiksDigisosId.zip")
        .body(soknadZip)
  }

  @GetMapping("{fiksDigisosId}/soknadJSON", produces = ["application/json"])
  fun getSoknadJsonV2(@PathVariable fiksDigisosId: String): JsonSoknad {
    val soknad =
        soknadService.hentSoknad(fiksDigisosId)
            ?: throw RuntimeException("Fant ikke søknad med id $fiksDigisosId")

    val soknadJson =
        objectMapper.readValue(
            soknadService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.metadata),
            JsonSoknad::class.java)
    return soknadJson
  }

  @GetMapping("{fiksDigisosId}/vedleggJSON", produces = ["application/json"])
  fun getVedleggJsonV2(@PathVariable fiksDigisosId: String): JsonVedleggSpesifikasjon {
    val soknad =
        soknadService.hentSoknad(fiksDigisosId)
            ?: throw RuntimeException("Fant ikke søknad med id $fiksDigisosId")

    val vedleggJson =
        objectMapper.readValue(
            soknadService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.vedleggMetadata),
            JsonVedleggSpesifikasjon::class.java)

    return vedleggJson
  }

  @GetMapping("{fiksDigisosId}/ettersendelseZip", produces = ["application/zip"])
  fun zipEttersendelseV2(@PathVariable fiksDigisosId: String): ResponseEntity<ByteArray> {
    val soknad =
        soknadService.hentSoknad(fiksDigisosId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    val ettersendelseZip = soknadArchiveService.makeEttersendelseZip(soknad)

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=ettersendelse_$fiksDigisosId.zip")
        .body(ettersendelseZip)
  }

  @GetMapping("")
  fun soknadsListeV2(): Collection<FrontendSoknad> =
      soknadService.listSoknader(null).map { toFrontendSoknad(it) }

  private fun toFrontendSoknad(soknad: DigisosSak) =
      FrontendSoknad(
          sokerFnr = soknad.sokerFnr,
          sokerNavn =
              runCatching { pdlService.getPersonalia(soknad.sokerFnr).navn.toString() }
                  .getOrDefault("<Ukjent>"),
          fiksDigisosId = soknad.fiksDigisosId,
          tittel = soknadService.hentSoknadstittel(soknad.fiksDigisosId),
          vedlegg = soknadService.hentVedlegg(soknad),
          vedleggSomMangler = soknadService.hentVedlegg(soknad).filter { !it.kanLastesned }.size)
}

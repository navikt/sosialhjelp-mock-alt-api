package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import jakarta.validation.Valid
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontoregisterService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.KrrService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.RolleService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skjermedepersoner.SkjermedePersonerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalDataService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.*
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendBarn.Companion.frontendBarn
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.aaregArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.pdlPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.Ettersendelse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FrontendController(
    private val pdlService: PdlService,
    private val skjermedePersonerService: SkjermedePersonerService,
    private val aaregService: AaregService,
    private val skatteetatenService: SkatteetatenService,
    private val bostotteService: BostotteService,
    private val utbetalDataService: UtbetalDataService,
    private val eregService: EregService,
    private val krrService: KrrService,
    private val soknadService: SoknadService,
    private val kontoregisterService: KontoregisterService,
    private val rolleService: RolleService,
    private val dokumentlagerService: DokumentlagerService,
) {
  companion object {
    private val log by logger()
  }

  // Frontend stuff:
  @PostMapping("/mock-alt/personalia")
  fun putMockPerson(@Valid @RequestBody personalia: FrontendPersonalia): ResponseEntity<String> {
    pdlService.veryfyNotLocked(personalia.fnr)
    personalia.barn.forEach { pdlService.leggTilBarn(it.fnr, it.pdlBarn()) }
    pdlService.leggTilPerson(pdlPersonalia(personalia))
    skjermedePersonerService.setStatus(personalia.fnr, personalia.skjerming)
    krrService.oppdaterKonfigurasjon(
        personalia.fnr, personalia.kanVarsles, personalia.epost, personalia.telefonnummer)
    if (personalia.kontonummer.isNotEmpty())
        kontoregisterService.putKonto(personalia.fnr, personalia.kontonummer)

    aaregService.setArbeidsforholdForFnr(
        personalia.fnr, personalia.arbeidsforhold.map { aaregArbeidsforhold(personalia.fnr, it) })
    personalia.arbeidsforhold.forEach {
      eregService.putOrganisasjonNoekkelinfo(it.orgnummer, it.orgnavn)
    }
    val skattbarInntektBuilder = SkattbarInntekt.Builder()
    personalia.skattetatenUtbetalinger.forEach {
      skattbarInntektBuilder.leggTilOppgave(FrontendSkattbarInntekt.oversettTilInntektsmottaker(it))
    }
    skatteetatenService.putSkattbarInntekt(personalia.fnr, skattbarInntektBuilder.build())
    val bostotteDto = BostotteDto()
    personalia.bostotteSaker.forEach { bostotteDto.saker.add(it) }
    personalia.bostotteUtbetalinger.forEach { bostotteDto.utbetalinger.add(it) }
    bostotteService.putBostotte(personalia.fnr, bostotteDto)
    utbetalDataService.putUtbetalingerFraNav(
        ident = personalia.fnr,
        utbetalinger = personalia.utbetalingerFraNav.map { it.toUtbetalDataDto() })
    rolleService.leggTilKonfigurasjon(personalia.fnr, personalia.administratorRoller)

    return ResponseEntity.ok("OK")
  }

  @GetMapping("/mock-alt/personalia")
  fun getMockPerson(@RequestParam ident: String): ResponseEntity<FrontendPersonalia> {
    val personalia =
        try {
          pdlService.getPersonalia(ident)
        } catch (e: MockAltException) {
          log.warn("Finner ikke personalia for fnr: $ident")
          return ResponseEntity.noContent().build()
        }
    log.info("Henter ned personalia for fnr: $ident")
    val frontendPersonalia = FrontendPersonalia(personalia)
    frontendPersonalia.skjerming = skjermedePersonerService.getStatus(ident)
    frontendPersonalia.barn =
        personalia.forelderBarnRelasjon.map { frontendBarn(it.ident, pdlService.getBarn(it.ident)) }
    val krrKonfigurasjon = krrService.hentKonfigurasjon(personalia.fnr)
    frontendPersonalia.telefonnummer = krrKonfigurasjon.mobiltelefonnummer ?: ""
    frontendPersonalia.epost = krrKonfigurasjon.epostadresse ?: ""
    frontendPersonalia.kanVarsles = krrKonfigurasjon.frontendKanVarsles()
    frontendPersonalia.kontonummer =
        kontoregisterService.getKonto(personalia.fnr)?.kontonummer ?: ""
    frontendPersonalia.arbeidsforhold =
        aaregService.getArbeidsforhold(personalia.fnr).map {
          FrontendArbeidsforhold.arbeidsforhold(it, eregService)
        }
    val skattbarInntekt = skatteetatenService.getSkattbarInntekt(personalia.fnr)
    frontendPersonalia.skattetatenUtbetalinger =
        skattbarInntekt.oppgaveInntektsmottaker.map { FrontendSkattbarInntekt.skattUtbetaling(it) }
    val bostotteDto = bostotteService.getBostotte(personalia.fnr)
    frontendPersonalia.bostotteSaker = bostotteDto.saker
    frontendPersonalia.bostotteUtbetalinger = bostotteDto.utbetalinger
    frontendPersonalia.utbetalingerFraNav =
        utbetalDataService.getUtbetalingerFraNav(personalia.fnr).toFrontend()
    frontendPersonalia.administratorRoller = rolleService.hentKonfigurasjon(personalia.fnr)

    return ResponseEntity.ok(frontendPersonalia)
  }

  @GetMapping("/mock-alt/personalia/liste")
  fun personListe(): ResponseEntity<Collection<Personalia>> {
    val personListe = pdlService.getPersonListe()
    return ResponseEntity.ok(personListe)
  }

  @GetMapping("/mock-alt/soknad/{fiksDigisosId}", produces = ["application/zip"])
  fun zipSoknad(@PathVariable fiksDigisosId: String): ResponseEntity<ByteArray> {
    val soknad = soknadService.hentSoknad(fiksDigisosId)!!
    val soknadsInfo = toFrontendSoknad(soknad)
    val bytebuffer = ByteArrayOutputStream()
    val zipArchive = ZipOutputStream(bytebuffer)

    val soknadJson = dokumentlagerService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.metadata)
    val soknadZip = ZipEntry("soknad.json")
    zipArchive.putNextEntry(soknadZip)
    zipArchive.write(soknadJson!!.toByteArray())
    zipArchive.closeEntry()

    val vedleggJson =
        dokumentlagerService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.vedleggMetadata)
    val vedleggZip = ZipEntry("vedlegg.json")
    zipArchive.putNextEntry(vedleggZip)
    zipArchive.write(vedleggJson!!.toByteArray())
    zipArchive.closeEntry()

    //        val forsendelseZip = ZipEntry("forsendelse.metadata.json")
    //        zipArchive.putNextEntry(forsendelseZip)
    //        zipArchive.write("{\"eksternRef\": \"$fiksDigisosId\", \"digisosId\":
    // \"$fiksDigisosId\"}".toByteArray())
    //        zipArchive.closeEntry()

    soknadsInfo.vedlegg
        .filterNot { vedlegg ->
          // filtrer vekk vedlegg sendt via innsyn
          vedlegg.id in
              (soknad.ettersendtInfoNAV?.ettersendelser?.map { it.vedleggMetadata } ?: emptyList())
        }
        .forEach { vedlegg ->
          val fil = dokumentlagerService.hentFil(vedlegg.id)
          if (fil != null) {
            val zipFile = ZipEntry(fil.filnavn)
            zipArchive.putNextEntry(zipFile)
            zipArchive.write(fil.bytes)
            zipArchive.closeEntry()
          }
        }
    zipArchive.finish()
    zipArchive.close()
    bytebuffer.close()
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=soknad_$fiksDigisosId.zip")
        .body(bytebuffer.toByteArray())
  }

  @GetMapping("/mock-alt/ettersendelse/{fiksDigisosId}", produces = ["application/zip"])
  fun zipEttersendelse(@PathVariable fiksDigisosId: String): ResponseEntity<ByteArray> {
    val soknad = soknadService.hentSoknad(fiksDigisosId)!!
    val bytebuffer = ByteArrayOutputStream()
    val zipArchive = ZipOutputStream(bytebuffer)

    val sammenslattVedleggJson =
        slaSammenTilJsonVedleggSpesifikasjon(
            soknad.ettersendtInfoNAV?.ettersendelser, fiksDigisosId)
    val vedleggZip = ZipEntry("vedlegg.json")
    zipArchive.putNextEntry(vedleggZip)
    zipArchive.write(objectMapper.writeValueAsBytes(sammenslattVedleggJson))
    zipArchive.closeEntry()

    val ettersendelsePdf = soknadService.hentEttersendelsePdf(fiksDigisosId)
    if (ettersendelsePdf != null) {
      val zipFile = ZipEntry(ettersendelsePdf.filnavn)
      zipArchive.putNextEntry(zipFile)
      zipArchive.write(ettersendelsePdf.bytes)
      zipArchive.closeEntry()
    }

    soknad.ettersendtInfoNAV
        ?.ettersendelser
        ?.flatMap { it.vedlegg }
        ?.forEach {
          val fil = dokumentlagerService.hentFil(it.dokumentlagerDokumentId)
          if (fil != null) {
            val zipFile = ZipEntry(fil.filnavn)
            zipArchive.putNextEntry(zipFile)
            zipArchive.write(fil.bytes)
            zipArchive.closeEntry()
          }
        }

    zipArchive.finish()
    zipArchive.close()
    bytebuffer.close()
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=ettersendelse_$fiksDigisosId.zip")
        .body(bytebuffer.toByteArray())
  }

  private fun slaSammenTilJsonVedleggSpesifikasjon(
      ettersendelser: List<Ettersendelse>?,
      fiksDigisosId: String
  ): JsonVedleggSpesifikasjon? {
    val vedleggSpesifikasjoner =
        ettersendelser
            ?.map { dokumentlagerService.hentDokument(fiksDigisosId, it.vedleggMetadata) }
            ?.map { objectMapper.readValue(it, JsonVedleggSpesifikasjon::class.java) }

    return JsonVedleggSpesifikasjon().withVedlegg(vedleggSpesifikasjoner?.flatMap { it.vedlegg })
  }

  @GetMapping("/mock-alt/soknad/liste")
  fun soknadsListe(): ResponseEntity<Collection<FrontendSoknad>> {
    return ResponseEntity.ok(soknadService.listSoknader(null).map { toFrontendSoknad(it) })
  }

  private fun toFrontendSoknad(soknad: DigisosSak): FrontendSoknad {
    soknadService.hentSoknadstittel(soknad.fiksDigisosId)
    val vedlegg = mutableListOf<FrontendVedlegg>()
    vedlegg.addAll(soknad.digisosSoker!!.dokumenter.map { toVedlegg(it) })
    soknad.ettersendtInfoNAV!!.ettersendelser.forEach { ettersendelse ->
      ettersendelse.vedlegg.forEach { vedlegg.add(toVedlegg(it)) }
    }
    val sokerNavn =
        try {
          pdlService.getPersonalia(soknad.sokerFnr).navn.toString()
        } catch (e: MockAltException) {
          "<Ukjent>"
        }

    return FrontendSoknad(
        sokerFnr = soknad.sokerFnr,
        sokerNavn = sokerNavn,
        fiksDigisosId = soknad.fiksDigisosId,
        tittel = soknadService.hentSoknadstittel(soknad.fiksDigisosId),
        vedlegg = vedlegg,
        vedleggSomMangler = vedlegg.filter { !it.kanLastesned }.size)
  }

  private fun toVedlegg(dokument: DokumentInfo): FrontendVedlegg {
    val kanLastesned = dokumentlagerService.hentFil(dokument.dokumentlagerDokumentId) != null
    return FrontendVedlegg(
        dokument.filnavn, dokument.dokumentlagerDokumentId, dokument.storrelse, kanLastesned)
  }
}

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import jakarta.validation.Valid
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontoregisterService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.KrrService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.RolleService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skjermedepersoner.SkjermedePersonerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalDataService
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendBarn.Companion.frontendBarn
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.aaregArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.pdlPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendSkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendUtbetalingFraNav.Companion.mapUtbetalingDtoListeTilFrontendUtbetalingerFraNavListe
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mock-alt/v2/brukere/")
class FrontendUserControllerV2(
    private val pdlService: PdlService,
    private val skjermedePersonerService: SkjermedePersonerService,
    private val aaregService: AaregService,
    private val skatteetatenService: SkatteetatenService,
    private val bostotteService: BostotteService,
    private val utbetalDataService: UtbetalDataService,
    private val eregService: EregService,
    private val krrService: KrrService,
    private val kontoregisterService: KontoregisterService,
    private val rolleService: RolleService,
) {
  companion object {
    private val log by logger()
  }

  @PostMapping
  fun postMockPerson(@Valid @RequestBody personalia: FrontendPersonalia): ResponseEntity<String> {
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

  @GetMapping fun listMockPersonerV2(): Collection<Personalia> = pdlService.getPersonListe()

  @GetMapping("{ident}")
  fun getMockPerson(@PathVariable ident: String): ResponseEntity<FrontendPersonalia> {
    val personalia =
        kotlin.runCatching { pdlService.getPersonalia(ident) }.getOrNull()
            ?: return ResponseEntity.notFound().build()

    log.info("Henter ned personalia for fnr: $ident")
    val krrKonfigurasjon = krrService.hentKonfigurasjon(personalia.fnr)
    val skattbarInntekt = skatteetatenService.getSkattbarInntekt(personalia.fnr)
    val bostotteDto = bostotteService.getBostotte(personalia.fnr)

    val frontendPersonalia = FrontendPersonalia(personalia)

    frontendPersonalia.apply {
      skjerming = skjermedePersonerService.getStatus(ident)
      barn =
          personalia.forelderBarnRelasjon.map {
            frontendBarn(it.ident, pdlService.getBarn(it.ident))
          }
      telefonnummer = krrKonfigurasjon.mobiltelefonnummer ?: ""
      epost = krrKonfigurasjon.epostadresse ?: ""
      kanVarsles = krrKonfigurasjon.frontendKanVarsles()
      kontonummer = kontoregisterService.getKonto(personalia.fnr)?.kontonummer ?: ""
      arbeidsforhold =
          aaregService.getArbeidsforhold(personalia.fnr).map {
            FrontendArbeidsforhold.arbeidsforhold(it, eregService)
          }
      skattetatenUtbetalinger =
          skattbarInntekt.oppgaveInntektsmottaker.map {
            FrontendSkattbarInntekt.skattUtbetaling(it)
          }
      bostotteSaker = bostotteDto.saker
      bostotteUtbetalinger = bostotteDto.utbetalinger
      utbetalingerFraNav =
          mapUtbetalingDtoListeTilFrontendUtbetalingerFraNavListe(
              utbetalDataService.getUtbetalingerFraNav(personalia.fnr))
      administratorRoller = rolleService.hentKonfigurasjon(personalia.fnr)
    }

    return ResponseEntity.ok(frontendPersonalia)
  }
}

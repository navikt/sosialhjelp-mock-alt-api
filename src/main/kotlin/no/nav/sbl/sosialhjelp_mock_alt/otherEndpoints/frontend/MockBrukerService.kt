package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontoregisterService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.KrrService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.RolleService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skjermedepersoner.SkjermedePersonerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalDataService
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.*
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class MockBrukerService(
    private val pdlService: PdlService,
    private val skjermedePersonerService: SkjermedePersonerService,
    private val aaregService: AaregService,
    private val skatteetatenService: SkatteetatenService,
    private val bostotteService: BostotteService,
    private val utbetalingerService: UtbetalDataService,
    private val eregService: EregService,
    private val krrService: KrrService,
    private val kontoregisterService: KontoregisterService,
    private val rolleService: RolleService,
) {
  companion object {
    private val log by logger()
  }

  fun getPerson(ident: String): FrontendPersonalia {
    log.info("Henter personalia for ident: $ident")
    val pdl = pdlService.getPersonalia(ident)
    val krr = krrService.hentKonfigurasjon(ident)
    val skatt = skatteetatenService.getSkattbarInntekt(ident)
    val husbank = bostotteService.getBostotte(ident)

    return FrontendPersonalia(pdl).apply {
      skjerming = skjermedePersonerService.getStatus(ident)
      barn =
          pdl.forelderBarnRelasjon.map {
            FrontendBarn.frontendBarn(it.ident, pdlService.getBarn(it.ident))
          }
      telefonnummer = krr.mobiltelefonnummer ?: ""
      epost = krr.epostadresse ?: ""
      kanVarsles = krr.frontendKanVarsles()
      kontonummer = kontoregisterService.getKonto(ident)?.kontonummer ?: ""
      arbeidsforhold =
          aaregService.getArbeidsforhold(ident).map {
            FrontendArbeidsforhold.arbeidsforhold(it, eregService)
          }
      skattetatenUtbetalinger =
          skatt.oppgaveInntektsmottaker.map { FrontendSkattbarInntekt.skattUtbetaling(it) }
      bostotteSaker = husbank.saker
      bostotteUtbetalinger = husbank.utbetalinger
      utbetalingerFraNav = utbetalingerService.getUtbetalingerFraNav(ident).toFrontend()
      administratorRoller = rolleService.hentKonfigurasjon(ident)
    }
  }

  fun newPerson(personalia: FrontendPersonalia) {
    log.info("Oppretter person med ident: $personalia.fnr")

    pdlService.veryfyNotLocked(personalia.fnr)
    personalia.barn.forEach { pdlService.leggTilBarn(it.fnr, it.pdlBarn()) }
    pdlService.leggTilPerson(FrontendPersonalia.pdlPersonalia(personalia))
    skjermedePersonerService.setStatus(personalia.fnr, personalia.skjerming)
    krrService.oppdaterKonfigurasjon(
        personalia.fnr, personalia.kanVarsles, personalia.epost, personalia.telefonnummer)
    if (personalia.kontonummer.isNotEmpty())
        kontoregisterService.putKonto(personalia.fnr, personalia.kontonummer)

    aaregService.setArbeidsforholdForFnr(
        personalia.fnr,
        personalia.arbeidsforhold.map {
          FrontendPersonalia.aaregArbeidsforhold(personalia.fnr, it)
        })
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
    utbetalingerService.putUtbetalingerFraNav(
        ident = personalia.fnr,
        utbetalinger = personalia.utbetalingerFraNav.map { it.toUtbetalDataDto() })
    rolleService.leggTilKonfigurasjon(personalia.fnr, personalia.administratorRoller)
  }
}

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.DkifService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.NavnDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.OrganisasjonNoekkelinfoDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendBarn
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendBarn.Companion.frontendBarn
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.pdlPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.aaregArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendSkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FrontendController(
        private val pdlService: PdlService,
        private val aaregService: AaregService,
        private val skatteetatenService: SkatteetatenService,
        private val bostotteService: BostotteService,
        private val eregService: EregService,
        private val dkifService: DkifService,
) {
    companion object {
        private val log by logger()
    }

    // Frontend stuff:
    @PostMapping("/mock-alt/personalia")
    fun frontendUpload(@RequestBody body: String): ResponseEntity<String> {
        log.info("Laster opp pdl data: $body")
        val personalia = objectMapper.readValue(body, FrontendPersonalia::class.java)
        if (personalia.fnr.isEmpty()) {
            return ResponseEntity.badRequest().body("FNR må være satt!")
        }
        pdlService.veryfyNotLocked(personalia.fnr)
        personalia.barn.forEach { pdlService.leggTilBarn(it.fnr, it.pdlBarn()) }
        pdlService.leggTilPerson(pdlPersonalia(personalia))
        if (personalia.telefonnummer.isNotEmpty()) {
            dkifService.putDigitalKontaktinfo(personalia.fnr, DigitalKontaktinfo(personalia.telefonnummer))
        }
        if (personalia.organisasjon.isNotEmpty() && personalia.organisasjonsNavn.isNotEmpty()) {
            eregService.putOrganisasjonNoekkelinfo(personalia.fnr,
                    OrganisasjonNoekkelinfoDto(
                            navn = NavnDto(personalia.organisasjonsNavn),
                            organisasjonsnummer = personalia.organisasjon,
                    )
            )
        }
        aaregService.setArbeidsforholdForFnr(
                personalia.fnr, personalia.arbeidsforhold.map { aaregArbeidsforhold(personalia.fnr, it) }
        )
        val skattbarInntektBuilder = SkattbarInntekt.Builder()
        personalia.skattetatenUtbetalinger.forEach {
            skattbarInntektBuilder.leggTilOppgave(FrontendSkattbarInntekt.oversettTilInntektsmottaker(it))
        }
        skatteetatenService.putSkattbarInntekt(personalia.fnr, skattbarInntektBuilder.build())
        val bostotteDto = BostotteDto()
        personalia.bostotteSaker.forEach { bostotteDto.saker.add(it) }
        personalia.bostotteUtbetalinger.forEach { bostotteDto.utbetalinger.add(it) }
        bostotteService.putBostotte(personalia.fnr, bostotteDto)
        return ResponseEntity.ok("OK")
    }

    @GetMapping("/mock-alt/personalia")
    fun pdlDownload(@RequestParam ident: String): ResponseEntity<FrontendPersonalia> {
        val personalia = try {
            pdlService.getPersonalia(ident)
        } catch (e: Exception) {
            log.warn("Finner ikke personalia for fnr: $ident")
            return ResponseEntity.noContent().build()
        }
        log.info("Henter ned pdl data for fnr: $ident")
        val frontendPersonalia = FrontendPersonalia(personalia)
        frontendPersonalia.barn =
                personalia.familierelasjon.map { frontendBarn(it.ident, pdlService.getBarn(it.ident)) }
        frontendPersonalia.telefonnummer =
                dkifService.getDigitalKontaktinfo(personalia.fnr)?.mobiltelefonnummer ?: ""
        frontendPersonalia.organisasjon =
                eregService.getOrganisasjonNoekkelinfo(personalia.fnr)?.organisasjonsnummer ?: ""
        frontendPersonalia.organisasjonsNavn =
                eregService.getOrganisasjonNoekkelinfo(personalia.fnr)?.navn?.navnelinje1 ?: ""
        frontendPersonalia.arbeidsforhold = aaregService.getArbeidsforhold(personalia.fnr)
                .map { FrontendArbeidsforhold.arbeidsforhold(it) }
        val skattbarInntekt = skatteetatenService.getSkattbarInntekt(personalia.fnr)
        frontendPersonalia.skattetatenUtbetalinger = skattbarInntekt.oppgaveInntektsmottaker.map {
            FrontendSkattbarInntekt.skattUtbetaling(it)
        }
        val bostotteDto = bostotteService.getBostotte(personalia.fnr)
        frontendPersonalia.bostotteSaker = bostotteDto.saker
        frontendPersonalia.bostotteUtbetalinger = bostotteDto.utbetalinger

        return ResponseEntity.ok(frontendPersonalia)
    }

    @GetMapping("/mock-alt/personalia/liste")
    fun personListe(): ResponseEntity<Collection<Personalia>> {
        val personListe = pdlService.getPersonListe()
        return ResponseEntity.ok(personListe)
    }
}

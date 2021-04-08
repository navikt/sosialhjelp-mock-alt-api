package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.DkifService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontonummerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalingService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingsListeDto
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendBarn.Companion.frontendBarn
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.aaregArbeidsforhold
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendPersonalia.Companion.pdlPersonalia
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendSkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendSoknad
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendUtbetalingFraNav.Companion.mapToFrontend
import no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model.FrontendVedlegg
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RestController
class FrontendController(
        private val pdlService: PdlService,
        private val aaregService: AaregService,
        private val skatteetatenService: SkatteetatenService,
        private val bostotteService: BostotteService,
        private val utbetalingService: UtbetalingService,
        private val eregService: EregService,
        private val dkifService: DkifService,
        private val soknadService: SoknadService,
        private val kontonummerService: KontonummerService,
) {
    companion object {
        private val log by logger()
    }

    // Frontend stuff:
    @PostMapping("/mock-alt/personalia")
    fun frontendUpload(@RequestBody body: String): ResponseEntity<String> {
        log.info("Laster opp personalia: $body")
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
        if (personalia.kontonummer.isNotEmpty()) {
            kontonummerService.putKontonummer(personalia.fnr, personalia.kontonummer)
        }
        aaregService.setArbeidsforholdForFnr(
                personalia.fnr, personalia.arbeidsforhold.map { aaregArbeidsforhold(personalia.fnr, it) }
        )
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
        utbetalingService.putUtbetalingerFraNav(personalia.fnr,
                UtbetalingsListeDto(personalia.utbetalingerFraNav.map { it.frontToBackend() }))
        return ResponseEntity.ok("OK")
    }

    @GetMapping("/mock-alt/personalia")
    fun frontendDownload(@RequestParam ident: String): ResponseEntity<FrontendPersonalia> {
        val personalia = try {
            pdlService.getPersonalia(ident)
        } catch (e: MockAltException) {
            log.warn("Finner ikke personalia for fnr: $ident")
            return ResponseEntity.noContent().build()
        }
        log.info("Henter ned personalia for fnr: $ident")
        val frontendPersonalia = FrontendPersonalia(personalia)
        frontendPersonalia.barn =
                personalia.forelderBarnRelasjon.map { frontendBarn(it.ident, pdlService.getBarn(it.ident)) }
        frontendPersonalia.telefonnummer =
                dkifService.getDigitalKontaktinfo(personalia.fnr)?.mobiltelefonnummer ?: ""
        frontendPersonalia.kontonummer = kontonummerService.getKontonummer(personalia.fnr)?.kontonummer ?: ""
        frontendPersonalia.arbeidsforhold = aaregService.getArbeidsforhold(personalia.fnr)
                .map { FrontendArbeidsforhold.arbeidsforhold(it, eregService) }
        val skattbarInntekt = skatteetatenService.getSkattbarInntekt(personalia.fnr)
        frontendPersonalia.skattetatenUtbetalinger = skattbarInntekt.oppgaveInntektsmottaker.map {
            FrontendSkattbarInntekt.skattUtbetaling(it)
        }
        val bostotteDto = bostotteService.getBostotte(personalia.fnr)
        frontendPersonalia.bostotteSaker = bostotteDto.saker
        frontendPersonalia.bostotteUtbetalinger = bostotteDto.utbetalinger
        frontendPersonalia.utbetalingerFraNav =
                utbetalingService.getUtbetalingerFraNav(personalia.fnr).utbetalinger.map { mapToFrontend(it) }

        return ResponseEntity.ok(frontendPersonalia)
    }

    @GetMapping("/mock-alt/personalia/liste")
    fun personListe(): ResponseEntity<Collection<Personalia>> {
        val personListe = pdlService.getPersonListe()
        return ResponseEntity.ok(personListe)
    }

    @GetMapping("/mock-alt/soknad/{fiksDigisosId}", produces = arrayOf("application/zip"))
    fun zipSoknad(@PathVariable fiksDigisosId: String): ResponseEntity<ByteArray> {
        val soknad = soknadService.hentSoknad(fiksDigisosId)!!
        val soknadsInfo = toFrontendSoknad(soknad)
        val bytebuffer = ByteArrayOutputStream()
        val zipArchive = ZipOutputStream(bytebuffer)

        val soknadJson = soknadService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.metadata)
        val soknadZip = ZipEntry("soknad.json")
        zipArchive.putNextEntry(soknadZip)
        zipArchive.write(soknadJson!!.toByteArray())
        zipArchive.closeEntry()

        val vedleggJson = soknadService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.vedleggMetadata)
        val vedleggZip = ZipEntry("vedlegg.json")
        zipArchive.putNextEntry(vedleggZip)
        zipArchive.write(vedleggJson!!.toByteArray())
        zipArchive.closeEntry()

        soknadsInfo.vedlegg.forEach { vedlegg ->
            val fil = soknadService.hentFil(vedlegg.id)
            if(fil != null) {
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
                .header("Content-Disposition",
                        "attachment; filename=soknad_$fiksDigisosId.zip")
                .body(bytebuffer.toByteArray())
    }

    @GetMapping("/mock-alt/soknad/liste")
    fun soknadsListe(): ResponseEntity<Collection<FrontendSoknad>> {
        return ResponseEntity.ok(soknadService.listSoknader(null).map { toFrontendSoknad(it) })
    }

    private fun toFrontendSoknad(soknad: DigisosSak) : FrontendSoknad {
        soknadService.hentSoknadstittel(soknad.fiksDigisosId)
        val vedlegg = mutableListOf<FrontendVedlegg>()
        vedlegg.addAll(soknad.digisosSoker!!.dokumenter.map { toVedlegg(it) })
        soknad.ettersendtInfoNAV!!.ettersendelser.forEach { ettersendelse ->
            ettersendelse.vedlegg.forEach { vedlegg.add(toVedlegg(it))}
        }
        val sokerNavn = try {
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
                vedleggSomMangler = vedlegg.filter { !it.kanLastesned }.size
        )
    }

    private fun toVedlegg(dokument: DokumentInfo) : FrontendVedlegg {
        val kanLastesned = soknadService.hentFil(dokument.dokumentlagerDokumentId) != null
        return FrontendVedlegg(dokument.filnavn, dokument.dokumentlagerDokumentId, dokument.storrelse, kanLastesned)
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten

import no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten.model.Forskuddstrekk
import no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten.model.Inntekt
import no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten.model.Inntektstype
import no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten.model.OppgaveInntektsmottaker
import no.nav.sbl.sosialhjelp_mock_alt.integrations.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SkatteetatenController {
    companion object {
        private val log by logger()
    }

    @GetMapping("/skatteetaten/{fnr}/oppgave/inntekt")
    fun getStatteetatenInntekt(
            @PathVariable fnr: String,
            @RequestParam fraOgMed: String,
            @RequestParam tilOgMed: String): ResponseEntity<SkattbarInntekt> {
        val id = randomInt(9).toString()
        val skattbarInntekt = SkattbarInntekt.Builder()
                .leggTilOppgave(
                        OppgaveInntektsmottaker.Builder()
                                .kalendermaaned("2020-08")
                                .opplysningspliktigId(id)
                                .leggTilForskuddstrekk(Forskuddstrekk.Builder().beloep(-200).build())
                                .leggTilInntekt(Inntekt.Builder()
                                        .skatteOgAvgiftsregel("hm...")
                                        .fordel("kontantytelse")
                                        .beloep(randomInt(5))
                                        .type(Inntektstype.Loennsinntekt)
                                        .build())
                                .leggTilForskuddstrekk(Forskuddstrekk.Builder()
                                        .beloep(-200)
                                        .build())
                                .build()
                )
                .leggTilOppgave(
                        OppgaveInntektsmottaker.Builder()
                                .kalendermaaned("2020-08")
                                .opplysningspliktigId(id)
                                .leggTilForskuddstrekk(Forskuddstrekk.Builder().beloep(-300).build())
                                .leggTilInntekt(Inntekt.Builder()
                                        .fordel("kontantytelse")
                                        .utloeserArbeidsgiveravgift(false)
                                        .inngaarIGrunnlagForTrekk(false)
                                        .beloep(randomInt(4))
                                        .type(Inntektstype.PensjonEllerTrygd, Inntektstype.DagmammaIEgenBolig)
                                        .build())
                                .leggTilForskuddstrekk(Forskuddstrekk.Builder()
                                        .beloep(-300)
                                        .build())
                                .build()
                )
                .leggTilOppgave(
                        OppgaveInntektsmottaker.Builder()
                                .kalendermaaned("2020-09")
                                .opplysningspliktigId(id)
                                .leggTilForskuddstrekk(Forskuddstrekk.Builder().beloep(-400).build())
                                .leggTilInntekt(Inntekt.Builder()
                                        .fordel("kontantytelse")
                                        .beloep(randomInt(5))
                                        .type(Inntektstype.Loennsinntekt)
                                        .build())
                                .leggTilForskuddstrekk(Forskuddstrekk.Builder()
                                        .beskrivelse("beskrivelse")
                                        .beloep(-400)
                                        .build())
                                .build()
                )
                .build()
        log.info("Henter skattbar inntekt: ${objectMapper.writeValueAsString(skattbarInntekt)}")
        return ResponseEntity.ok(skattbarInntekt)
    }
}

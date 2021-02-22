package no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model

import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SkattbarInntekt(val oppgaveInntektsmottaker: MutableList<OppgaveInntektsmottaker> = mutableListOf()) {
    data class Builder(
            var oppgaver: MutableList<OppgaveInntektsmottaker> = mutableListOf()
    ) {
        fun leggTilOppgave(oppgave: OppgaveInntektsmottaker) = apply { oppgaver.add(oppgave) }

        fun build() = SkattbarInntekt(oppgaver)
    }
}

class OppgaveInntektsmottaker(
        val kalendermaaned: String,
        val opplysningspliktigId: String,
        val inntekt: List<Inntekt>,
        val forskuddstrekk: List<Forskuddstrekk>,
) {
    data class Builder(
            var kalendermaaned: String = "",
            var opplysningspliktigId: String? = null,
            val inntektsListe: MutableList<Inntekt> = mutableListOf(),
            val forskuddstrekksListe: MutableList<Forskuddstrekk> = mutableListOf(),
    ) {
        fun kalendermaaned(kalendermaaned: String) = apply { this.kalendermaaned = kalendermaaned }
        fun opplysningspliktigId(opplysningspliktigId: String) = apply { this.opplysningspliktigId = opplysningspliktigId }
        fun leggTilInntekt(inntekt: Inntekt) = apply { inntektsListe.add(inntekt) }
        fun leggTilForskuddstrekk(trekk: Forskuddstrekk) = apply { forskuddstrekksListe.add(trekk) }
        fun standardOppgave(
                dato: LocalDate = LocalDate.now().minusDays(14),
                belop: Int = 15000,
                trekk: Int = 5000
        ) = apply {
            kalendermaaned = DateTimeFormatter.ofPattern("yyyy-MM").format(dato)
            opplysningspliktigId = randomInt(5).toString()
            leggTilInntekt(
                    Inntekt.Builder()
                            .type(Inntektstype.Loennsinntekt)
                            .beloep(belop)
                            .build()
            )
            leggTilForskuddstrekk(Forskuddstrekk.Builder().beloep(trekk).build())
        }

        fun build() = OppgaveInntektsmottaker(kalendermaaned, opplysningspliktigId!!, inntektsListe, forskuddstrekksListe)
    }
}

class Forskuddstrekk(
        val beskrivelse: String,
        val beloep: Int,
) {
    data class Builder(
            var beskrivelse: String = "",
            var beloep: Int = 0,
    ) {
        fun beskrivelse(beskrivelse: String) = apply { this.beskrivelse = beskrivelse }
        fun beloep(beloep: Int) = apply { this.beloep = beloep }

        fun build() = Forskuddstrekk(beskrivelse, beloep)
    }
}

class Inntekt(
        val skatteOgAvgiftsregel: String,
        val fordel: String,
        val utloeserArbeidsgiveravgift: Boolean,
        val inngaarIGrunnlagForTrekk: Boolean,
        val beloep: Int,
        val loennsinntekt: Loennsinntekt?,
        val ytelseFraOffentlige: YtelseFraOffentlige?,
        val pensjonEllerTrygd: PensjonEllerTrygd?,
        val lottOgPartInnenFiske: LottOgPartInnenFiske?,
        val dagmammaIEgenBolig: DagmammaIEgenBolig?,
        val naeringsinntekt: Naeringsinntekt?,
        val aldersUfoereEtterlatteAvtalefestetOgKrigspensjon: AldersUfoereEtterlatteAvtalefestetOgKrigspensjon?,
) {
    fun type(): Inntektstype {
        return when {
            loennsinntekt != null -> Inntektstype.Loennsinntekt
            ytelseFraOffentlige != null -> Inntektstype.YtelseFraOffentlige
            pensjonEllerTrygd != null -> Inntektstype.PensjonEllerTrygd
            lottOgPartInnenFiske != null -> Inntektstype.LottOgPartInnenFiske
            dagmammaIEgenBolig != null -> Inntektstype.DagmammaIEgenBolig
            naeringsinntekt != null -> Inntektstype.Naeringsinntekt
            aldersUfoereEtterlatteAvtalefestetOgKrigspensjon != null -> Inntektstype.AldersUfoereEtterlatteAvtalefestetOgKrigspensjon
            else -> Inntektstype.Loennsinntekt
        }
    }

    data class Builder(
            var skatteOgAvgiftsregel: String = "",
            var fordel: String = "",
            var utloeserArbeidsgiveravgift: Boolean = true,
            var inngaarIGrunnlagForTrekk: Boolean = true,
            var beloep: Int = 0,
            var loennsinntekt: Loennsinntekt? = null,
            var ytelseFraOffentlige: YtelseFraOffentlige? = null,
            var pensjonEllerTrygd: PensjonEllerTrygd? = null,
            var lottOgPartInnenFiske: LottOgPartInnenFiske? = null,
            var dagmammaIEgenBolig: DagmammaIEgenBolig? = null,
            var naeringsinntekt: Naeringsinntekt? = null,
            var aldersUfoereEtterlatteAvtalefestetOgKrigspensjon: AldersUfoereEtterlatteAvtalefestetOgKrigspensjon? = null,
    ) {
        fun skatteOgAvgiftsregel(skatteOgAvgiftsregel: String) = apply { this.skatteOgAvgiftsregel = skatteOgAvgiftsregel }
        fun fordel(fordel: String) = apply { this.fordel = fordel }
        fun utloeserArbeidsgiveravgift(utloeserArbeidsgiveravgift: Boolean) = apply { this.utloeserArbeidsgiveravgift = utloeserArbeidsgiveravgift }
        fun inngaarIGrunnlagForTrekk(inngaarIGrunnlagForTrekk: Boolean) = apply { this.inngaarIGrunnlagForTrekk = inngaarIGrunnlagForTrekk }
        fun beloep(beloep: Int) = apply { this.beloep = beloep }

        fun type(type: Inntektstype, subType: Inntektstype? = null) = apply {
            when (type) {
                Inntektstype.Loennsinntekt -> loennsinntekt = Loennsinntekt(
                        Tilleggsinformasjon.Builder().ofType(subType).build())
                Inntektstype.YtelseFraOffentlige -> ytelseFraOffentlige = YtelseFraOffentlige(
                        Tilleggsinformasjon.Builder().ofType(subType).build())
                Inntektstype.PensjonEllerTrygd -> pensjonEllerTrygd = PensjonEllerTrygd(
                        Tilleggsinformasjon.Builder().ofType(subType).build())
                Inntektstype.LottOgPartInnenFiske -> lottOgPartInnenFiske = LottOgPartInnenFiske()
                Inntektstype.DagmammaIEgenBolig -> dagmammaIEgenBolig = DagmammaIEgenBolig()
                Inntektstype.Naeringsinntekt -> naeringsinntekt = Naeringsinntekt()
                Inntektstype.AldersUfoereEtterlatteAvtalefestetOgKrigspensjon ->
                    aldersUfoereEtterlatteAvtalefestetOgKrigspensjon = AldersUfoereEtterlatteAvtalefestetOgKrigspensjon()
            }
        }

        fun build() = Inntekt(
                skatteOgAvgiftsregel,
                fordel,
                utloeserArbeidsgiveravgift,
                inngaarIGrunnlagForTrekk,
                beloep,
                loennsinntekt,
                ytelseFraOffentlige,
                pensjonEllerTrygd,
                lottOgPartInnenFiske,
                dagmammaIEgenBolig,
                naeringsinntekt,
                aldersUfoereEtterlatteAvtalefestetOgKrigspensjon,
        )
    }
}

enum class Inntektstype {
    Loennsinntekt,
    YtelseFraOffentlige,
    PensjonEllerTrygd,
    LottOgPartInnenFiske,
    DagmammaIEgenBolig,
    Naeringsinntekt,
    AldersUfoereEtterlatteAvtalefestetOgKrigspensjon,
}

class Loennsinntekt(
        val tilleggsinformasjon: Tilleggsinformasjon
)

class YtelseFraOffentlige(
        val tilleggsinformasjon: Tilleggsinformasjon
)

class PensjonEllerTrygd(
        val tilleggsinformasjon: Tilleggsinformasjon
)

class Tilleggsinformasjon(
        val dagmammaIEgenBolig: DagmammaIEgenBolig?,
        val lottOgPart: LottOgPartInnenFiske?,
        val pensjon: AldersUfoereEtterlatteAvtalefestetOgKrigspensjon?,
) {
    data class Builder(
            var dagmammaIEgenBolig: DagmammaIEgenBolig? = null,
            var lottOgPart: LottOgPartInnenFiske? = null,
            var pensjon: AldersUfoereEtterlatteAvtalefestetOgKrigspensjon? = null,
    ) {
        fun ofType(type: Inntektstype? = null) = apply {
            if (type != null) {
                when (type) {
                    Inntektstype.DagmammaIEgenBolig -> dagmammaIEgenBolig = DagmammaIEgenBolig()
                    Inntektstype.LottOgPartInnenFiske -> lottOgPart = LottOgPartInnenFiske()
                    Inntektstype.AldersUfoereEtterlatteAvtalefestetOgKrigspensjon ->
                        pensjon = AldersUfoereEtterlatteAvtalefestetOgKrigspensjon()
                    else -> {
                    }
                }
            }
        }

        fun build() = Tilleggsinformasjon(dagmammaIEgenBolig, lottOgPart, pensjon)
    }
}

class LottOgPartInnenFiske

class DagmammaIEgenBolig

class Naeringsinntekt

class AldersUfoereEtterlatteAvtalefestetOgKrigspensjon

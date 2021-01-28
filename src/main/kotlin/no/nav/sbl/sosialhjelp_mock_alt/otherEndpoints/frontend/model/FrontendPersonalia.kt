package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model

import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.SakerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.UtbetalingerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.ForenkletBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Gradering
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Forskuddstrekk
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Inntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Inntektstype
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.OppgaveInntektsmottaker
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsgiverType
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.OpplysningspliktigArbeidsgiverDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.OrganisasjonDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.PersonDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.toIsoString
import java.time.LocalDate

data class FrontendPersonalia(
        val fnr: String = genererTilfeldigPersonnummer(),
        val navn: PdlPersonNavn = PdlPersonNavn(),
        var addressebeskyttelse: Gradering = Gradering.UGRADERT,
        var sivilstand: String = "UOPPGITT",
        var ektefelle: String? = null,
        var starsborgerskap: String = "NOR",
        var bostedsadresse: ForenkletBostedsadresse = ForenkletBostedsadresse("Hovedveien", 42, "0101", "0301"),
        var telefonnummer: String = "",
        var organisasjon: String = "",
        var organisasjonsNavn: String = "",
        var arbeidsforhold: List<FrontendArbeidsforhold>,
        var bostotteSaker: List<SakerDto>,
        var bostotteUtbetalinger: List<UtbetalingerDto>,
        var skattetatenUtbetalinger: List<FrontendSkattbarInntekt>,
        var locked: Boolean = false,
) {
    constructor(personalia: Personalia) : this(
            fnr = personalia.fnr,
            navn = personalia.navn,
            addressebeskyttelse = personalia.addressebeskyttelse,
            sivilstand = personalia.sivilstand,
            ektefelle = personalia.ektefelle,
            starsborgerskap = personalia.starsborgerskap,
            bostedsadresse = personalia.bostedsadresse,
            telefonnummer = "",
            organisasjon = "",
            organisasjonsNavn = "",
            arbeidsforhold = emptyList(),
            bostotteSaker = emptyList(),
            bostotteUtbetalinger = emptyList(),
            skattetatenUtbetalinger = emptyList(),
            locked = personalia.locked,
    )

    companion object {
        fun pdlPersonalia(personalia: FrontendPersonalia): Personalia {
            return Personalia(
                    fnr = personalia.fnr,
                    navn = personalia.navn,
                    addressebeskyttelse = personalia.addressebeskyttelse,
                    sivilstand = personalia.sivilstand,
                    ektefelle = personalia.ektefelle,
                    starsborgerskap = personalia.starsborgerskap,
                    bostedsadresse = personalia.bostedsadresse,
                    locked = personalia.locked,
            )
        }

        fun aaregArbeidsforhold(fnr: String, frontendArbeidsforhold: FrontendArbeidsforhold): ArbeidsforholdDto {
            val arbeidsgiver: OpplysningspliktigArbeidsgiverDto
            if (frontendArbeidsforhold.type == ArbeidsgiverType.Person.name) {
                arbeidsgiver = PersonDto(frontendArbeidsforhold.ident, frontendArbeidsforhold.ident)
            } else {
                arbeidsgiver = OrganisasjonDto(frontendArbeidsforhold.orgnummer)
            }
            return ArbeidsforholdDto.nyttArbeidsforhold(
                    fnr = fnr,
                    fom = textToLocalDate(frontendArbeidsforhold.startDato),
                    tom = textToLocalDate(frontendArbeidsforhold.sluttDato),
                    stillingsprosent = frontendArbeidsforhold.stillingsProsent.toDouble(),
                    arbeidsforholdId = frontendArbeidsforhold.id,
                    arbeidsgiver = arbeidsgiver,
            )
        }

        private fun textToLocalDate(string: String): LocalDate {
            return LocalDate.of(string.substring(0, 4).toInt(), string.substring(5, 7).toInt(), string.substring(8).toInt())
        }
    }
}

class FrontendSkattbarInntekt(
        val beloep: String,
        val trekk: String,
        val orgnummer: String,
        val maned: String,
        val type: Inntektstype,
) {
    companion object {
        fun oversettTilInntektsmottaker(frontEnd: FrontendSkattbarInntekt): OppgaveInntektsmottaker {
            return OppgaveInntektsmottaker.Builder()
                    .kalendermaaned(frontEnd.maned)
                    .opplysningspliktigId(frontEnd.orgnummer)
                    .leggTilForskuddstrekk(Forskuddstrekk.Builder().beloep(-frontEnd.trekk.toInt()).build())
                    .leggTilInntekt(Inntekt.Builder()
                            .skatteOgAvgiftsregel("hm...")
                            .fordel("kontantytelse")
                            .beloep(frontEnd.beloep.toInt())
                            .type(frontEnd.type)
                            .build())
                    .build()
        }

        fun skattUtbetaling(backend: OppgaveInntektsmottaker): FrontendSkattbarInntekt {
            return FrontendSkattbarInntekt(
                    beloep = backend.inntekt[0].beloep.toString(),
                    trekk = backend.forskuddstrekk[0].beloep.toString(),
                    orgnummer = backend.opplysningspliktigId,
                    maned = backend.kalendermaaned,
                    type = backend.inntekt[0].type(),
            )
        }

    }
}

class FrontendArbeidsforhold(
        val type: String,
        val id: String,
        val startDato: String,
        val sluttDato: String,
        val stillingsProsent: String,
        val ident: String,
        val orgnummer: String,
) {
    companion object {
        fun arbeidsforhold(dto: ArbeidsforholdDto): FrontendArbeidsforhold {
            var sluttDato = ""
            if (dto.ansettelsesperiode.periode.tom != null) sluttDato = dto.ansettelsesperiode.periode.tom.toIsoString()
            var ident = ""
            var orgnummer = ""
            if (dto.arbeidsgiver is OrganisasjonDto) {
                orgnummer = dto.arbeidsgiver.organisasjonsnummer
            }
            if (dto.arbeidsgiver is PersonDto) {
                ident = dto.arbeidsgiver.offentligIdent
            }
            return FrontendArbeidsforhold(
                    type = dto.arbeidsgiver.type,
                    id = dto.arbeidsforholdId,
                    startDato = dto.ansettelsesperiode.periode.fom.toIsoString(),
                    sluttDato = sluttDato,
                    stillingsProsent = dto.arbeidsavtaler[0].stillingsprosent.toString(),
                    ident = ident,
                    orgnummer = orgnummer,
            )
        }
    }
}
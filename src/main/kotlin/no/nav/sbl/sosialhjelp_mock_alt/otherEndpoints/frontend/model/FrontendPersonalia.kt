package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model

import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.SakerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.UtbetalingerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Adressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Familierelasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.ForenkletBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Gradering
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFoedsel
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFolkeregisterpersonstatus
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadBarn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlVegadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Forskuddstrekk
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Inntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Inntektstype
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.OppgaveInntektsmottaker
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.ArbeidsgiverType
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.OpplysningspliktigArbeidsgiverDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.OrganisasjonDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.PersonDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import no.nav.sbl.sosialhjelp_mock_alt.utils.toIsoString
import java.time.LocalDate
import java.util.Date

data class FrontendPersonalia(
        val fnr: String = genererTilfeldigPersonnummer(),
        val navn: PdlPersonNavn = PdlPersonNavn(),
        var addressebeskyttelse: Gradering = Gradering.UGRADERT,
        var sivilstand: String = "UOPPGITT",
        var ektefelle: String? = null,
        var barn: List<FrontendBarn>,
        var starsborgerskap: String = "NOR",
        var bostedsadresse: ForenkletBostedsadresse = ForenkletBostedsadresse("Gateveien", 1, "0101", "0301"),
        var telefonnummer: String = "",
        var organisasjon: String = "",
        var organisasjonsNavn: String = "",
        var arbeidsforhold: List<FrontendArbeidsforhold>,
        var bostotteSaker: List<SakerDto>,
        var bostotteUtbetalinger: List<UtbetalingerDto>,
        var skattetatenUtbetalinger: List<FrontendSkattbarInntekt>,
        var utbetalingerFraNav: List<FrontendUtbetalingFraNav>,
        var locked: Boolean = false,
) {
    constructor(personalia: Personalia) : this(
            fnr = personalia.fnr,
            navn = personalia.navn,
            addressebeskyttelse = personalia.addressebeskyttelse,
            sivilstand = personalia.sivilstand,
            ektefelle = personalia.ektefelle,
            barn = emptyList(),
            starsborgerskap = personalia.starsborgerskap,
            bostedsadresse = personalia.bostedsadresse,
            telefonnummer = "",
            organisasjon = "",
            organisasjonsNavn = "",
            arbeidsforhold = emptyList(),
            bostotteSaker = emptyList(),
            bostotteUtbetalinger = emptyList(),
            skattetatenUtbetalinger = emptyList(),
            utbetalingerFraNav = emptyList(),
            locked = personalia.locked,
    )

    companion object {
        fun pdlPersonalia(personalia: FrontendPersonalia): Personalia {
            val familierelasjoner = personalia.barn.map { Familierelasjon(it.fnr, "barn", "forelder") }
            return Personalia(
                    fnr = personalia.fnr,
                    navn = personalia.navn,
                    addressebeskyttelse = personalia.addressebeskyttelse,
                    sivilstand = personalia.sivilstand,
                    ektefelle = personalia.ektefelle,
                    familierelasjon = familierelasjoner,
                    starsborgerskap = personalia.starsborgerskap,
                    bostedsadresse = personalia.bostedsadresse,
                    locked = personalia.locked,
            )
        }

        fun aaregArbeidsforhold(fnr: String, frontendArbeidsforhold: FrontendArbeidsforhold): ArbeidsforholdDto {
            val arbeidsgiver: OpplysningspliktigArbeidsgiverDto = when (frontendArbeidsforhold.type) {
                ArbeidsgiverType.Person.name -> {
                    PersonDto(frontendArbeidsforhold.ident, frontendArbeidsforhold.ident)
                }
                ArbeidsgiverType.Organisasjon.name -> {
                    OrganisasjonDto(frontendArbeidsforhold.orgnummer)
                }
                else -> {
                    throw MockAltException("Ukjent ArbreidsgiverType: ${frontendArbeidsforhold.type}")
                }
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

data class FrontendBarn(
        val fnr: String,
        var addressebeskyttelse: Gradering = Gradering.UGRADERT,
        var bostedsadresse: ForenkletBostedsadresse = ForenkletBostedsadresse("Hovedveien", 42, "0101", "0301"),
        var folkeregisterpersonstatus: String = "bosatt",
        val foedsel: LocalDate = LocalDate.now().minusYears(10),
        val navn: PdlPersonNavn = PdlPersonNavn(),
) {

    fun pdlBarn(): PdlSoknadBarn {
        val vegadresse = PdlVegadresse(
                randomInt(7).toString(),
                bostedsadresse.adressenavn,
                bostedsadresse.husnummer,
                bostedsadresse.adressenavn,
                null,
                bostedsadresse.postnummer,
                bostedsadresse.kommunenummer,
                null,
        )
        return PdlSoknadBarn(
                adressebeskyttelse = listOf(Adressebeskyttelse(addressebeskyttelse)),
                bostedsadresse = listOf(PdlBostedsadresse(null, vegadresse, null, null)),
                folkeregisterpersonstatus = listOf(PdlFolkeregisterpersonstatus(folkeregisterpersonstatus)),
                foedsel = listOf(PdlFoedsel(foedsel)),
                navn = listOf(PdlSoknadPersonNavn(navn.fornavn, navn.mellomnavn, navn.etternavn)),
        )
    }

    companion object {
        fun frontendBarn(fnr: String, pdlBarn: PdlSoknadBarn): FrontendBarn {
            val bostedsadresse = pdlBarn.bostedsadresse!!.first()
            val navn = pdlBarn.navn?.first() ?: PdlSoknadPersonNavn("", "", "")
            return FrontendBarn(
                    fnr = fnr,
                    addressebeskyttelse = pdlBarn.adressebeskyttelse!!.first().gradering,
                    bostedsadresse = ForenkletBostedsadresse(
                            adressenavn = bostedsadresse.vegadresse?.adressenavn ?: "",
                            husnummer = bostedsadresse.vegadresse?.husnummer ?: 1,
                            postnummer = bostedsadresse.vegadresse?.postnummer ?: "",
                            kommunenummer = bostedsadresse.vegadresse?.kommunenummer ?: "",
                    ),
                    folkeregisterpersonstatus = pdlBarn.folkeregisterpersonstatus?.first()?.status ?: "bosatt",
                    foedsel = pdlBarn.foedsel?.first()?.foedselsdato ?: LocalDate.now().minusYears(10),
                    navn = PdlPersonNavn(navn.fornavn, navn.mellomnavn, navn.etternavn)
            )
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

class FrontendUtbetalingFraNav(
        private val belop: Double,
        private val dato: Date,
        private val ytelsestype: String,
) {
    fun frontToBackend(): UtbetalingDto {
        return UtbetalingDto(belop, dato, ytelsestype)
    }
    companion object {
        fun mapToFrontend(utbetaling: UtbetalingDto): FrontendUtbetalingFraNav {
            return FrontendUtbetalingFraNav(utbetaling.belop, utbetaling.dato, utbetaling.ytelsestype)
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

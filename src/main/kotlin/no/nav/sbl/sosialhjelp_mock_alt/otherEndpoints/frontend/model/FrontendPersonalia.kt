package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend.model

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.ArbeidsgiverType
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.OpplysningspliktigArbeidsgiverDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.OrganisasjonDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.model.PersonDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.SakerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.UtbetalingerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Adressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.ForelderBarnRelasjon
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
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.model.AdminRolle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Forskuddstrekk
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Inntekt
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.Inntektstype
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.OppgaveInntektsmottaker
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.KomponentDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.UtbetalDataDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.Ytelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.Ytelseskomponent
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import no.nav.sbl.sosialhjelp_mock_alt.utils.toIsoString
import java.math.BigDecimal
import java.time.LocalDate

data class FrontendPersonalia(
    val fnr: String = genererTilfeldigPersonnummer(),
    val navn: PdlPersonNavn = PdlPersonNavn(),
    var adressebeskyttelse: Gradering = Gradering.UGRADERT,
    var skjerming: Boolean = false,
    var sivilstand: String = "UOPPGITT",
    var ektefelle: String? = null,
    var barn: List<FrontendBarn>,
    var starsborgerskap: String = "NOR",
    var bostedsadresse: ForenkletBostedsadresse = ForenkletBostedsadresse(
        adressenavn = "Gateveien",
        husnummer = 1,
        postnummer = "0101",
        kommunenummer = "0301"
    ),
    var telefonnummer: String = "",
    var epost: String = "",
    var kanVarsles: Boolean = true,
    var kontonummer: String = "",
    var arbeidsforhold: List<FrontendArbeidsforhold>,
    var bostotteSaker: List<SakerDto>,
    var bostotteUtbetalinger: List<UtbetalingerDto>,
    var skattetatenUtbetalinger: List<FrontendSkattbarInntekt>,
    var utbetalingerFraNav: List<FrontendUtbetalingFraNav>,
    var administratorRoller: List<AdminRolle>,
    var locked: Boolean = false,
) {
    constructor(personalia: Personalia) : this(
        fnr = personalia.fnr,
        navn = personalia.navn,
        adressebeskyttelse = personalia.adressebeskyttelse,
        sivilstand = personalia.sivilstand,
        ektefelle = personalia.ektefelleType,
        barn = emptyList(),
        starsborgerskap = personalia.starsborgerskap,
        bostedsadresse = personalia.bostedsadresse,
        telefonnummer = "",
        kontonummer = "",
        arbeidsforhold = emptyList(),
        bostotteSaker = emptyList(),
        bostotteUtbetalinger = emptyList(),
        skattetatenUtbetalinger = emptyList(),
        utbetalingerFraNav = emptyList(),
        administratorRoller = emptyList(),
        locked = personalia.locked,
    )

    companion object {
        fun pdlPersonalia(personalia: FrontendPersonalia): Personalia {
            val forelderBarnRelasjon = personalia.barn.map { ForelderBarnRelasjon(it.fnr, "barn", "forelder") }
            return Personalia(
                fnr = personalia.fnr,
                navn = personalia.navn,
                adressebeskyttelse = personalia.adressebeskyttelse,
                sivilstand = personalia.sivilstand,
                ektefelleType = personalia.ektefelle,
                forelderBarnRelasjon = forelderBarnRelasjon,
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
            return LocalDate.of(
                string.substring(0, 4).toInt(),
                string.substring(5, 7).toInt(),
                string.substring(8).toInt()
            )
        }
    }
}

data class FrontendBarn(
    val fnr: String,
    var adressebeskyttelse: Gradering = Gradering.UGRADERT,
    var bostedsadresse: ForenkletBostedsadresse = ForenkletBostedsadresse(
        adressenavn = "Hovedveien",
        husnummer = 42,
        postnummer = "0101",
        kommunenummer = "0301"
    ),
    var folkeregisterpersonstatus: String = "bosatt",
    val foedsel: LocalDate = LocalDate.now().minusYears(10),
    val navn: PdlPersonNavn = PdlPersonNavn(),
) {

    fun pdlBarn(): PdlSoknadBarn {
        val vegadresse = PdlVegadresse(
            matrikkelId = randomInt(7).toString(),
            adressenavn = bostedsadresse.adressenavn,
            husnummer = bostedsadresse.husnummer,
            husbokstav = if (bostedsadresse.husbokstav.isNullOrBlank()) null else bostedsadresse.husbokstav,
            tilleggsnavn = null,
            postnummer = bostedsadresse.postnummer,
            kommunenummer = bostedsadresse.kommunenummer,
            bruksenhetsnummer = null,
        )
        return PdlSoknadBarn(
            adressebeskyttelse = listOf(Adressebeskyttelse(adressebeskyttelse)),
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
                adressebeskyttelse = pdlBarn.adressebeskyttelse!!.first().gradering,
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
                .leggTilInntekt(
                    Inntekt.Builder()
                        .skatteOgAvgiftsregel("hm...")
                        .fordel("kontantytelse")
                        .beloep(frontEnd.beloep.toInt())
                        .type(frontEnd.type)
                        .build()
                )
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

data class FrontendUtbetalingFraNav(
    val belop: Double,
    val dato: LocalDate,
    val ytelsestype: String,
    val melding: String,
    val skattebelop: Double,
    val ytelseskomponenttype: String,
) {

    fun toUtbetalingDto(): UtbetalingDto {
        return UtbetalingDto(
            tittel = ytelsestype,
            netto = belop,
            skattetrekk = skattebelop,
            utbetalingsdato = dato,
            komponenter = listOf(KomponentDto(type = ytelseskomponenttype))
        )
    }

    fun toUtbetalDataDto(): UtbetalDataDto {
        return UtbetalDataDto(
            ytelseListe = listOf(
                Ytelse(
                    ytelsestype = ytelsestype,
                    skattsum = BigDecimal(skattebelop),
                    ytelseskomponentListe = listOf(Ytelseskomponent(ytelseskomponenttype = ytelseskomponenttype))
                )
            ),
            utbetalingNettobeloep = BigDecimal(belop),
            utbetalingsdato = dato,
            utbetalingsmelding = melding,
        )
    }

    companion object {
        fun mapUtbetalingDtoToFrontendUtbelingFraNav(utbetaling: UtbetalingDto): FrontendUtbetalingFraNav {
            return FrontendUtbetalingFraNav(
                utbetaling.netto,
                utbetaling.utbetalingsdato ?: LocalDate.now(),
                utbetaling.tittel,
                "", // melding?
                utbetaling.skattetrekk,
                utbetaling.komponenter.first().type ?: ""
            )
        }

        // TODO: bytt ut metode over med denne når vi har skrudd over til utbetalData. Pt ubrukt.
        fun mapUtbetalDataDtoToFrontendUtbelingFraNav(utbetalData: UtbetalDataDto): FrontendUtbetalingFraNav {

            // TODO  en del verdier her som er én verdi kommer fra lister. Velger nå første i liste, men hvordan skal logikken her henge sammen?

            var skattsum = 0.00

            for (ytelse in utbetalData.ytelseListe!!) {
                skattsum += ytelse.skattsum?.toDouble() ?: 0.0
            }

            return FrontendUtbetalingFraNav(
                utbetalData.utbetalingNettobeloep?.toDouble() ?: 0.00,
                utbetalData.utbetalingsdato ?: LocalDate.now(),
                utbetalData.ytelseListe.first().ytelsestype ?: "",
                utbetalData.utbetalingsmelding ?: "",
                skattsum,
                utbetalData.ytelseListe.first().ytelseskomponentListe?.first()?.ytelseskomponenttype ?: ""
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
    val orgnavn: String,
) {
    companion object {
        fun arbeidsforhold(dto: ArbeidsforholdDto, eregService: EregService): FrontendArbeidsforhold {
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
            val orgnavn = eregService.getOrganisasjonNoekkelinfo(orgnummer)?.navn?.navnelinje1 ?: ""
            return FrontendArbeidsforhold(
                type = dto.arbeidsgiver.type,
                id = dto.arbeidsforholdId,
                startDato = dto.ansettelsesperiode.periode.fom.toIsoString(),
                sluttDato = sluttDato,
                stillingsProsent = dto.arbeidsavtaler[0].stillingsprosent.toString(),
                ident = ident,
                orgnummer = orgnummer,
                orgnavn = orgnavn,
            )
        }
    }
}

package no.nav.sbl.sosialhjelp.mock.alt.otherEndpoints.frontend.model

import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsforholdResponseDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsstedDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidsstedType
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.ArbeidstakerIdentType
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.IdentInfoDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.aareg.model.IdentInfoType
import no.nav.sbl.sosialhjelp.mock.alt.datastore.bostotte.model.SakerDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.bostotte.model.UtbetalingerDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.Adressebeskyttelse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.Folkeregisterpersonstatus
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.ForelderBarnRelasjon
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.ForenkletBostedsadresse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.Gradering
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlBostedsadresse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlFoedsel
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlFolkeregisterpersonstatus
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlSoknadBarn
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlSoknadPersonNavn
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.PdlVegadresse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp.mock.alt.datastore.roller.model.AdminRolle
import no.nav.sbl.sosialhjelp.mock.alt.datastore.skatteetaten.model.Forskuddstrekk
import no.nav.sbl.sosialhjelp.mock.alt.datastore.skatteetaten.model.Inntekt
import no.nav.sbl.sosialhjelp.mock.alt.datastore.skatteetaten.model.Inntektstype
import no.nav.sbl.sosialhjelp.mock.alt.datastore.skatteetaten.model.OppgaveInntektsmottaker
import no.nav.sbl.sosialhjelp.mock.alt.datastore.utbetaling.model.UtbetalDataDto
import no.nav.sbl.sosialhjelp.mock.alt.datastore.utbetaling.model.Ytelse
import no.nav.sbl.sosialhjelp.mock.alt.datastore.utbetaling.model.Ytelseskomponent
import no.nav.sbl.sosialhjelp.mock.alt.utils.MockAltException
import no.nav.sbl.sosialhjelp.mock.alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp.mock.alt.utils.randomInt
import no.nav.sbl.sosialhjelp.mock.alt.utils.toIsoString
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
    var bostedsadresse: ForenkletBostedsadresse =
        ForenkletBostedsadresse(
            adressenavn = "Sannergata",
            husnummer = 1,
            postnummer = "0557",
            kommunenummer = "0301",
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
    constructor(
        personalia: Personalia,
    ) : this(
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
            val forelderBarnRelasjon =
                personalia.barn.map { ForelderBarnRelasjon(it.fnr, "barn", "forelder") }
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

        fun aaregArbeidsforhold(
            fnr: String,
            frontendArbeidsforhold: FrontendArbeidsforhold,
        ): ArbeidsforholdResponseDto {
            val arbeidssted: ArbeidsstedDto =
                when (frontendArbeidsforhold.type) {
                    ArbeidsstedType.Person -> createArbeidssted(ArbeidsstedType.Person, frontendArbeidsforhold.ident)
                    ArbeidsstedType.Underenhet ->
                        createArbeidssted(
                            ArbeidsstedType.Underenhet,
                            frontendArbeidsforhold.orgnummer,
                        )

                    else -> throw MockAltException("Ukjent ArbreidsgiverType: ${frontendArbeidsforhold.type}")
                }
            return ArbeidsforholdResponseDto.createNyttArbeidsforhold(
                personId = fnr,
                start = textToLocalDate(frontendArbeidsforhold.startDato),
                slutt = textToLocalDate(frontendArbeidsforhold.sluttDato),
                stillingsprosent = frontendArbeidsforhold.stillingsProsent ?: 0.0,
                arbeidssted = arbeidssted,
            )
        }

        private fun createArbeidssted(
            type: ArbeidsstedType,
            ident: String,
        ): ArbeidsstedDto =
            ArbeidsstedDto(
                type = type,
                identer =
                    listOf(
                        IdentInfoDto(
                            type =
                                if (type == ArbeidsstedType.Person) {
                                    IdentInfoType.FOLKEREGISTERIDENT
                                } else {
                                    IdentInfoType.ORGANISASJONSNUMMER
                                },
                            ident = ident,
                            gjeldende = true,
                        ),
                    ),
            )

        private fun textToLocalDate(string: String): LocalDate =
            LocalDate.of(
                string.substring(0, 4).toInt(),
                string.substring(5, 7).toInt(),
                string.substring(8).toInt(),
            )
    }
}

data class FrontendBarn(
    val fnr: String,
    var adressebeskyttelse: Gradering = Gradering.UGRADERT,
    var bostedsadresse: ForenkletBostedsadresse =
        ForenkletBostedsadresse(
            adressenavn = "Hovedveien",
            husnummer = 42,
            postnummer = "0101",
            kommunenummer = "0301",
        ),
    var folkeregisterpersonstatus: PdlFolkeregisterpersonstatus =
        PdlFolkeregisterpersonstatus(Folkeregisterpersonstatus.bosatt),
    val foedsel: LocalDate = LocalDate.now().minusYears(10),
    val navn: PdlPersonNavn = PdlPersonNavn(),
) {
    fun pdlBarn(): PdlSoknadBarn {
        val vegadresse =
            PdlVegadresse(
                matrikkelId = randomInt(7).toString(),
                adressenavn = bostedsadresse.adressenavn,
                husnummer = bostedsadresse.husnummer,
                husbokstav =
                    if (bostedsadresse.husbokstav.isNullOrBlank()) null else bostedsadresse.husbokstav,
                tilleggsnavn = null,
                postnummer = bostedsadresse.postnummer,
                kommunenummer = bostedsadresse.kommunenummer,
                bruksenhetsnummer = null,
            )
        return PdlSoknadBarn(
            adressebeskyttelse = listOf(Adressebeskyttelse(adressebeskyttelse)),
            bostedsadresse = listOf(PdlBostedsadresse(null, vegadresse, null, null)),
            folkeregisterpersonstatus = listOf(folkeregisterpersonstatus),
            foedsel = listOf(PdlFoedsel(foedsel)),
            navn = listOf(PdlSoknadPersonNavn(navn.fornavn, navn.mellomnavn, navn.etternavn)),
        )
    }

    companion object {
        fun frontendBarn(
            fnr: String,
            pdlBarn: PdlSoknadBarn,
        ): FrontendBarn {
            val bostedsadresse = pdlBarn.bostedsadresse!!.first()
            val navn = pdlBarn.navn?.first() ?: PdlSoknadPersonNavn("", "", "")
            return FrontendBarn(
                fnr = fnr,
                adressebeskyttelse = pdlBarn.adressebeskyttelse!!.first().gradering,
                bostedsadresse =
                    ForenkletBostedsadresse(
                        adressenavn = bostedsadresse.vegadresse?.adressenavn ?: "",
                        husnummer = bostedsadresse.vegadresse?.husnummer ?: 1,
                        postnummer = bostedsadresse.vegadresse?.postnummer ?: "",
                        kommunenummer = bostedsadresse.vegadresse?.kommunenummer ?: "",
                    ),
                folkeregisterpersonstatus =
                    pdlBarn.folkeregisterpersonstatus?.firstOrNull()
                        ?: PdlFolkeregisterpersonstatus(Folkeregisterpersonstatus.bosatt),
                foedsel = pdlBarn.foedsel?.first()?.foedselsdato ?: LocalDate.now().minusYears(10),
                navn = PdlPersonNavn(navn.fornavn, navn.mellomnavn, navn.etternavn),
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
        fun oversettTilInntektsmottaker(frontEnd: FrontendSkattbarInntekt): OppgaveInntektsmottaker =
            OppgaveInntektsmottaker
                .Builder()
                .kalendermaaned(frontEnd.maned)
                .opplysningspliktigId(frontEnd.orgnummer)
                .leggTilForskuddstrekk(Forskuddstrekk.Builder().beloep(-frontEnd.trekk.toInt()).build())
                .leggTilInntekt(
                    Inntekt
                        .Builder()
                        .skatteOgAvgiftsregel("hm...")
                        .fordel("kontantytelse")
                        .beloep(frontEnd.beloep.toInt())
                        .type(frontEnd.type)
                        .build(),
                ).build()

        fun skattUtbetaling(backend: OppgaveInntektsmottaker): FrontendSkattbarInntekt =
            FrontendSkattbarInntekt(
                beloep = backend.inntekt[0].beloep.toString(),
                trekk = backend.forskuddstrekk[0].beloep.toString(),
                orgnummer = backend.opplysningspliktigId,
                maned = backend.kalendermaaned,
                type = backend.inntekt[0].type(),
            )
    }
}

data class FrontendUtbetalingFraNav(
    val belop: Double,
    val dato: LocalDate,
    val ytelsestype: String,
    val skattebelop: Double,
    val ytelseskomponenttype: String,
) {
    fun toUtbetalDataDto(): UtbetalDataDto =
        UtbetalDataDto(
            utbetalingsdato = dato,
            ytelseListe =
                listOf(
                    Ytelse(
                        ytelsestype = ytelsestype,
                        ytelseNettobeloep = BigDecimal(belop),
                        skattsum = BigDecimal(skattebelop),
                        ytelseskomponentListe =
                            listOf(Ytelseskomponent(ytelseskomponenttype = ytelseskomponenttype)),
                    ),
                ),
        )
}

fun List<UtbetalDataDto>.toFrontend(): List<FrontendUtbetalingFraNav> =
    this.flatMap { utbetalingFraNav ->
        utbetalingFraNav.ytelseListe?.map {
            FrontendUtbetalingFraNav(
                belop = it.ytelseNettobeloep?.toDouble() ?: 0.00,
                dato = utbetalingFraNav.utbetalingsdato ?: LocalDate.now(),
                ytelsestype = it.ytelsestype ?: "",
                skattebelop = it.skattsum?.toDouble() ?: 0.00,
                ytelseskomponenttype = it.ytelseskomponentListe?.first()?.ytelseskomponenttype ?: "",
            )
        } ?: emptyList()
    }

class FrontendArbeidsforhold(
    val type: ArbeidsstedType?,
    val id: String,
    val startDato: String,
    val sluttDato: String,
    val stillingsProsent: Double?,
    val ident: String,
    val orgnummer: String,
    val orgnavn: String,
) {
    companion object {
        fun arbeidsforhold(
            dto: ArbeidsforholdResponseDto,
            eregService: EregService,
        ): FrontendArbeidsforhold {
            val orgnummer =
                dto.arbeidssted
                    ?.identer
                    ?.find { it.type == IdentInfoType.ORGANISASJONSNUMMER }
                    ?.ident
            val orgnavn = orgnummer?.let { eregService.getOrganisasjonNoekkelinfo(it)?.navn?.navnelinje1 }
            val ident =
                dto.arbeidstaker
                    ?.identer
                    ?.find { it.type == ArbeidstakerIdentType.FOLKEREGISTERIDENT }
                    ?.ident
            return arbeidsforhold(dto, orgnavn, orgnummer, ident)
        }

        fun arbeidsforhold(
            dto: ArbeidsforholdResponseDto,
            orgnavn: String?,
            orgnummer: String?,
            ident: String?,
        ) = FrontendArbeidsforhold(
            type = dto.arbeidssted?.type,
            id = dto.id,
            startDato =
                dto.ansettelsesperiode.startdato
                    .toIsoString(),
            sluttDato =
                dto.ansettelsesperiode.sluttdato
                    ?.toIsoString() ?: "",
            stillingsProsent = dto.ansettelsesdetaljer?.find { it.rapporteringsmaaneder?.til == null }?.avtaltStillingsprosent,
            ident = ident ?: "",
            orgnummer = orgnummer ?: "",
            orgnavn = orgnavn ?: "",
        )
    }
}

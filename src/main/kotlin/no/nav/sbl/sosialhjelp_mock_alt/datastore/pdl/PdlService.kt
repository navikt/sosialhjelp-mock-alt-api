package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.BostotteService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.EregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.KontonummerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.KrrService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Adressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.ForenkletBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Gradering
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Kjoenn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFoedsel
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFoedselsdato
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFolkeregisterpersonstatus
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlForelderBarnRelasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlKjoenn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSivilstand
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadAdressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadAdressebeskyttelseResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadBarn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadBarnResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadEktefelle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadEktefelleResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadHentAdressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadHentBarn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadHentEktefelle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlStatsborgerskap
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlTelefonnummer
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlVegadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.SivilstandType
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.RolleService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.model.AdminRolle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.SkatteetatenService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalDataService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.UtbetalingService
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.fastFnr
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigKontonummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigOrganisasjonsnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigTelefonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomDate
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PdlService(
    private val eregService: EregService,
    private val aaregService: AaregService,
    private val skatteetatenService: SkatteetatenService,
    private val utbetalingService: UtbetalingService,
    private val utbetalDataService: UtbetalDataService,
    private val bostotteService: BostotteService,
    private val soknadService: SoknadService,
    private val kontonummerService: KontonummerService,
    private val pdlGeografiskTilknytningService: PdlGeografiskTilknytningService,
    private val krrService: KrrService,
    private val rolleService: RolleService,
) {

    private val personListe: HashMap<String, Personalia> = HashMap()
    private val ektefelleMap = mutableMapOf<String, PdlSoknadEktefelle>()
    private val barnMap = mutableMapOf<String, PdlSoknadBarn>()

    init {
        opprettBrukerMedAlt(fastFnr, "Standard", "Standardsen", "NOR", 1, adminRoller = AdminRolle.values().asList())
        val bergenFnr = opprettBrukerMedAlt(
            genererTilfeldigPersonnummer(), "Bergen", "Bergenhusen", "NOR", 2,
            postnummer = "5005", kommuneNummer = "4601", enhetsnummer = "1209"
        )
        krrService.oppdaterKonfigurasjon(bergenFnr, kanVarsles = false)
        opprettBrukerMedAlt(genererTilfeldigPersonnummer(), "Tyske", "Tyskersen", "GER", 3)
        opprettBrukerMedAlt(genererTilfeldigPersonnummer(), "Admin", "Adminsen", "NOR", 4, adminRoller = listOf(AdminRolle.MODIA_VEILEDER))

        val hemmeligBruker = Personalia()
            .withNavn("Hemmelig", "", "Adressesen")
            .withAdressebeskyttelse(Gradering.STRENGT_FORTROLIG)
            .withOpprettetTidspunkt(5)
            .locked()
        personListe[hemmeligBruker.fnr] = hemmeligBruker
    }

    fun getInnsynResponseFor(ident: String): PdlInnsynPersonResponse {
        log.info("Henter PDL innsyns data for $ident")
        val personalia = personListe[ident]
        var adressebeskyttelseList: List<Adressebeskyttelse> = emptyList()
        var navnList: List<PdlPersonNavn> = listOf(PdlPersonNavn("Ukjent", "PDL", "Person"))
        if (personalia != null) {
            adressebeskyttelseList = listOf(Adressebeskyttelse(personalia.adressebeskyttelse))
            navnList = listOf(PdlPersonNavn(personalia.navn.fornavn, personalia.navn.mellomnavn, personalia.navn.etternavn))
        }
        return PdlInnsynPersonResponse(
            errors = null,
            data = PdlInnsynHentPerson(
                hentPerson = PdlInnsynPerson(
                    adressebeskyttelse = adressebeskyttelseList,
                    navn = navnList
                )
            )
        )
    }

    fun getModiaResponseFor(ident: String): PdlModiaPersonResponse {
        log.info("Henter PDL modia data for $ident")
        val personalia = personListe[ident]
        var navn = PdlPersonNavn("Person", "", "Testperson")
        var adressebeskyttelse = Gradering.UGRADERT
        val kjoenn = PdlKjoenn(Kjoenn.KVINNE)
        val foedselsdato = PdlFoedselsdato("1945-10-26")
        val telefonnummer = PdlTelefonnummer("+47", "11112222", 1)
        if (personalia != null) {
            adressebeskyttelse = personalia.adressebeskyttelse
            navn = personalia.navn
        }
        return PdlModiaPersonResponse(
            errors = null,
            data = PdlModiaHentPerson(
                hentPerson = PdlModiaPerson(
                    adressebeskyttelse = listOf(Adressebeskyttelse(adressebeskyttelse)),
                    navn = listOf(navn),
                    kjoenn = listOf(kjoenn),
                    foedsel = listOf(foedselsdato),
                    telefonnummer = listOf(telefonnummer)
                )
            )
        )
    }

    fun getSoknadPersonResponseFor(ident: String): PdlSoknadPersonResponse {
        log.info("Henter PDL soknad data for (person) $ident")

        val personalia = personListe[ident]
        var navn = PdlSoknadPersonNavn("Person", "", "Testperson")
        var forelderBarnRelasjon: List<PdlForelderBarnRelasjon> = emptyList()
        var sivilstand = PdlSivilstand(SivilstandType.UGIFT, null)
        var statsborgerskap = PdlStatsborgerskap("NOR")
        var bostedsadresse = PdlBostedsadresse(null, defaultAdresse, null, null)

        if (personalia != null) {
            navn = PdlSoknadPersonNavn(personalia.navn.fornavn, personalia.navn.mellomnavn, personalia.navn.etternavn)
            if (personalia.sivilstand.equals("GIFT", true) || personalia.sivilstand.equals("PARTNER", true)) {
                if (personalia.ektefelleFnr.isNullOrEmpty()) {
                    leggTilEktefelle(personalia)
                }
                sivilstand = PdlSivilstand(SivilstandType.valueOf(personalia.sivilstand), personalia.ektefelleFnr)
            }
            forelderBarnRelasjon = personalia.forelderBarnRelasjon.map { PdlForelderBarnRelasjon(it.ident, it.rolle, it.motrolle) }
            statsborgerskap = PdlStatsborgerskap(personalia.starsborgerskap)
            bostedsadresse = PdlBostedsadresse(
                null,
                PdlVegadresse(
                    "matrikkelId",
                    personalia.bostedsadresse.adressenavn,
                    personalia.bostedsadresse.husnummer,
                    if (personalia.bostedsadresse.husbokstav.isNullOrBlank()) null else personalia.bostedsadresse.husbokstav,
                    null,
                    personalia.bostedsadresse.postnummer,
                    personalia.bostedsadresse.kommunenummer,
                    null,
                ),
                null, null
            )
        }

        return PdlSoknadPersonResponse(
            errors = null,
            data = PdlSoknadHentPerson(
                hentPerson = PdlSoknadPerson(
                    bostedsadresse = listOf(bostedsadresse),
                    kontaktadresse = emptyList(),
                    oppholdsadresse = emptyList(),
                    forelderBarnRelasjon = forelderBarnRelasjon,
                    navn = listOf(navn),
                    sivilstand = listOf(sivilstand),
                    statsborgerskap = listOf(statsborgerskap)
                )
            )
        )
    }

    fun getSoknadEktefelleResponseFor(ident: String): PdlSoknadEktefelleResponse {
        log.info("Henter PDL soknad data for (ektefelle) $ident")

        val pdlEktefelle = ektefelleMap[ident] ?: defaultEktefelle(randomDate())

        return PdlSoknadEktefelleResponse(
            errors = null,
            data = PdlSoknadHentEktefelle(
                hentPerson = pdlEktefelle
            )
        )
    }

    fun getSoknadBarnResponseFor(ident: String): PdlSoknadBarnResponse {
        log.info("Henter PDL soknad data for (barn) $ident")

        val pdlBarn = barnMap[ident] ?: defaultBarn()

        return PdlSoknadBarnResponse(
            errors = null,
            data = PdlSoknadHentBarn(
                hentPerson = pdlBarn
            )
        )
    }

    fun getSoknadAdressebeskyttelseResponseFor(ident: String): PdlSoknadAdressebeskyttelseResponse {
        log.info("Henter PDL adressebeskyttelse for $ident")

        val personalia = personListe[ident]
        var adressebeskyttelse = Adressebeskyttelse(Gradering.UGRADERT)
        if (personalia != null) {
            adressebeskyttelse = Adressebeskyttelse(personalia.adressebeskyttelse)
        }

        return PdlSoknadAdressebeskyttelseResponse(
            errors = null,
            data = PdlSoknadHentAdressebeskyttelse(
                hentPerson = PdlSoknadAdressebeskyttelse(adressebeskyttelse = listOf(adressebeskyttelse))
            )
        )
    }

    // Util:

    fun leggTilPerson(personalia: Personalia) {
        leggTilEktefelle(personalia)
        if (personListe[personalia.fnr] != null && personListe[personalia.fnr]!!.locked) {
            throw MockAltException("Ident ${personalia.fnr} is locked! Cannot update!")
        }
        personListe[personalia.fnr] = personalia
        pdlGeografiskTilknytningService.putGeografiskTilknytning(personalia.fnr, personalia.bostedsadresse.kommunenummer)
    }

    private fun leggTilEktefelle(personalia: Personalia) {
        personalia.ektefelleFodselsdato = randomDate()
        val fnr = genererTilfeldigPersonnummer(personalia.ektefelleFodselsdato)
        personalia.ektefelleFnr = fnr
        when (personalia.ektefelleType) {
            "EKTEFELLE_SAMME_BOSTED" -> ektefelleMap[fnr] = ektefelleSammeBosted(personalia.ektefelleFodselsdato)
            "EKTEFELLE_ANNET_BOSTED" -> ektefelleMap[fnr] = ektefelleAnnetBosted(personalia.ektefelleFodselsdato)
            "EKTEFELLE_MED_ADRESSEBESKYTTELSE" -> ektefelleMap[fnr] = ektefelleMedAdressebeskyttelse
            else -> ektefelleMap[fnr] = defaultEktefelle(personalia.ektefelleFodselsdato)
        }
    }

    fun leggTilBarn(fnr: String, pdlBarn: PdlSoknadBarn) {
        barnMap[fnr] = pdlBarn
    }

    fun getPersonalia(ident: String): Personalia {
        return personListe[ident] ?: throw MockAltException("Ident $ident not found!")
    }

    fun getBarn(ident: String): PdlSoknadBarn {
        return barnMap[ident] ?: throw MockAltException("Barn with ident $ident not found!")
    }

    fun veryfyNotLocked(fnr: String) {
        val personalia = personListe[fnr]
        if (personalia != null && personalia.locked) {
            throw MockAltException("Bruker er låst og skal ikke oppdateres!")
        }
    }

    fun getPersonListe(): List<Personalia> {
        return personListe.values.sortedBy { it.opprettetTidspunkt }
    }

    fun finnesPersonMedFnr(fnr: String?): Boolean {
        return personListe.containsKey(key = fnr)
    }

    private fun opprettBrukerMedAlt(
        brukerFnr: String,
        fornavn: String,
        etternavn: String,
        statsborgerskap: String,
        position: Long,
        postnummer: String = "0101",
        kommuneNummer: String = "0301",
        enhetsnummer: String = "0315",
        adminRoller: List<AdminRolle> = emptyList()
    ): String {
        val barnFnr = genererTilfeldigPersonnummer()
        val standardBruker = Personalia(fnr = brukerFnr)
            .withNavn(fornavn, "", etternavn)
            .withOpprettetTidspunkt(position)
            .withEktefelleType("EKTEFELLE_SAMME_BOSTED")
            .withEktefelleFodselsDato(randomDate())
            .withSivilstand("GIFT")
            .withForelderBarnRelasjon(barnFnr)
            .withBostedsadresse(
                ForenkletBostedsadresse(
                    adressenavn = "Gateveien",
                    husnummer = 1,
                    postnummer = postnummer,
                    kommunenummer = kommuneNummer
                )
            )
            .withStarsborgerskap(statsborgerskap)
            .locked()
        personListe[brukerFnr] = standardBruker
        ektefelleMap[brukerFnr] = ektefelleSammeBosted(standardBruker.ektefelleFodselsdato)
        barnMap[barnFnr] = defaultBarn(etternavn)

        pdlGeografiskTilknytningService.putGeografiskTilknytning(brukerFnr, standardBruker.bostedsadresse.kommunenummer)
        krrService.oppdaterKonfigurasjon(brukerFnr, true, telefonnummer = genererTilfeldigTelefonnummer())
        kontonummerService.putKontonummer(brukerFnr, genererTilfeldigKontonummer())
        val organisasjonsnummer = genererTilfeldigOrganisasjonsnummer()
        eregService.putOrganisasjonNoekkelinfo(organisasjonsnummer, "Arbeidsgiveren AS")
        aaregService.leggTilEnkeltArbeidsforhold(
            personalia = standardBruker,
            startDato = LocalDate.now().minusYears(10),
            orgnummmer = organisasjonsnummer,
        )

        skatteetatenService.enableAutoGenerationFor(brukerFnr)
        utbetalingService.enableAutoGenerationFor(brukerFnr)
        utbetalDataService.enableAutoGenerationFor(brukerFnr)
        bostotteService.enableAutoGenerationFor(brukerFnr)
        rolleService.leggTilKonfigurasjon(brukerFnr, adminRoller)

        soknadService.opprettDigisosSak(enhetsnummer, kommuneNummer, brukerFnr, brukerFnr)
        if (fornavn == "Standard") soknadService.opprettDigisosSak(enhetsnummer, kommuneNummer, brukerFnr, "15months")
        return brukerFnr
    }

    companion object {
        private val log by logger()

        private val defaultAdresse = PdlVegadresse("matrikkelId", "Gateveien", 1, "A", null, "0101", "0301", "H101")
        private val annenAdresse = PdlVegadresse("matrikkelId2", "Karl Johans gate", 1, null, null, "0101", "0301", null)

        private fun ektefelleSammeBosted(dato: LocalDate) = PdlSoknadEktefelle(
            adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
            bostedsadresse = listOf(PdlBostedsadresse(null, defaultAdresse, null, null)),
            foedsel = listOf(PdlFoedsel(dato)),
            navn = listOf(PdlSoknadPersonNavn("LILLA", "", "EKTEFELLE"))
        )

        private fun ektefelleAnnetBosted(dato: LocalDate) = PdlSoknadEktefelle(
            adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
            bostedsadresse = listOf(PdlBostedsadresse(null, annenAdresse, null, null)),
            foedsel = listOf(PdlFoedsel(dato)),
            navn = listOf(PdlSoknadPersonNavn("GUL", "", "EKTEFELLE"))
        )

        private val ektefelleMedAdressebeskyttelse = PdlSoknadEktefelle(
            adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.FORTROLIG)),
            bostedsadresse = emptyList(),
            foedsel = emptyList(),
            navn = emptyList()
        )

        private fun defaultEktefelle(dato: LocalDate) =
            PdlSoknadEktefelle(
                adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                bostedsadresse = listOf(PdlBostedsadresse(null, defaultAdresse, null, null)),
                foedsel = listOf(PdlFoedsel(dato)),
                navn = listOf(PdlSoknadPersonNavn("Ektefelle", "", "McEktefelle"))
            )

        private fun defaultBarn(etternavn: String = "McKid") =
            PdlSoknadBarn(
                adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                bostedsadresse = listOf(PdlBostedsadresse(null, defaultAdresse, null, null)),
                folkeregisterpersonstatus = listOf(PdlFolkeregisterpersonstatus("bosatt")),
                foedsel = listOf(PdlFoedsel(LocalDate.now().minusYears(10))),
                navn = listOf(PdlSoknadPersonNavn("Kid", "", etternavn))
            )
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.aareg.AaregService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Adressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Gradering
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Kjoenn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlBostedsadresse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlEndring
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFamilierelasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFoedsel
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFoedselsdato
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFolkeregistermetadata
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFolkeregisterpersonstatus
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlKjoenn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlMetadata
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSivilstand
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadBarn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadBarnResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadEktefelle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlSoknadEktefelleResponse
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
import no.nav.sbl.sosialhjelp_mock_alt.utils.fastFnr
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PdlService(aaregService: AaregService) {

    final val personListe: HashMap<String, Personalia> = HashMap()

    init {
        val standardBruker = Personalia(fnr = fastFnr)
                .withNavn("Standard", "", "Standarsen")
                .withOpprettetTidspunkt(0)
                .locked()
        personListe.put(fastFnr, standardBruker)
        aaregService.leggTilEnkeltArbeidsforhold(personalia = standardBruker, LocalDate.now().minusYears(10))
        val hemmeligBruker = Personalia()
                .withNavn("Hemmelig", "", "Adressesen")
                .withAdressebeskyttelse(Gradering.STRENGT_FORTROLIG)
                .withOpprettetTidspunkt(1)
                .locked()
        personListe.put(hemmeligBruker.fnr, hemmeligBruker)
        aaregService.leggTilEnkeltArbeidsforhold(personalia = hemmeligBruker, LocalDate.now().minusYears(10))
        val svenskBruker = Personalia()
                .withNavn("Svenske", "", "Svenskersen")
                .withStarsborgerskap("SWE")
                .withOpprettetTidspunkt(2)
                .locked()
        personListe.put(svenskBruker.fnr, svenskBruker)
        aaregService.leggTilEnkeltArbeidsforhold(personalia = svenskBruker, LocalDate.now().minusYears(10))
    }

    private val ektefelleMap = mutableMapOf<String, PdlSoknadEktefelle>()
    private val barnMap = mutableMapOf<String, PdlSoknadBarn>()

    fun getInnsynResponseFor(ident: String): PdlInnsynPersonResponse {
        log.info("Henter PDL innsyns data for $ident")
        val personalia = personListe[ident]
        var adressebeskyttelseList: List<Adressebeskyttelse> = emptyList()
        var navnList: List<PdlNavn> = emptyList()
        if (personalia != null) {
            adressebeskyttelseList = listOf(Adressebeskyttelse(personalia.addressebeskyttelse))
            navnList = listOf(PdlNavn(personalia.navn.fornavn))
        }
        return PdlInnsynPersonResponse(
                errors = emptyList(),
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
        val kjoenn = PdlKjoenn(Kjoenn.KVINNE)
        val foedselsdato = PdlFoedselsdato("1945-10-26")
        val telefonnummer = PdlTelefonnummer("+47", "11112222", 1)
        if (personalia != null) {
            navn = personalia.navn
        }
        return PdlModiaPersonResponse(
                errors = emptyList(),
                data = PdlModiaHentPerson(
                        hentPerson = PdlModiaPerson(
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
        var adressebeskyttelse = Adressebeskyttelse(Gradering.UGRADERT)
        var navn = PdlSoknadPersonNavn("Person", "", "Testperson")
        var familierelasjoner: List<PdlFamilierelasjon> = emptyList()
        var sivilstand = PdlSivilstand(SivilstandType.UGIFT, null)
        var statsborgerskap = PdlStatsborgerskap("NOR")
        var bostedsadresse = PdlBostedsadresse(null, defaultAdresse, null, null)

        if (personalia != null) {
            navn = PdlSoknadPersonNavn(personalia.navn.fornavn, personalia.navn.mellomnavn, personalia.navn.etternavn)
            adressebeskyttelse = Adressebeskyttelse(personalia.addressebeskyttelse)
            if (personalia.sivilstand.equals("GIFT", true) || personalia.sivilstand.equals("PARTNER", true)) {
                val ektefelleIdent = genererTilfeldigPersonnummer()
                sivilstand = PdlSivilstand(SivilstandType.valueOf(personalia.sivilstand), ektefelleIdent)
                when (personalia.ektefelle) {
                    "EKTEFELLE_SAMME_BOSTED" -> ektefelleMap[ident] = ektefelleSammeBosted
                    "EKTEFELLE_ANNET_BOSTED" -> ektefelleMap[ident] = ektefelleAnnetBosted
                    "EKTEFELLE_MED_ADRESSEBESKYTTELSE" -> ektefelleMap[ident] = ektefelleMedAdressebeskyttelse
                }
            }
            familierelasjoner = personalia.familierelasjon.map { PdlFamilierelasjon(it.ident, it.rolle, it.motrolle) }
            statsborgerskap = PdlStatsborgerskap(personalia.starsborgerskap)
            bostedsadresse = PdlBostedsadresse(null,
                    PdlVegadresse(
                            "matrikkelId",
                            personalia.bostedsadresse.adressenavn,
                            personalia.bostedsadresse.husnummer,
                            null,
                            null,
                            personalia.bostedsadresse.postnummer,
                            personalia.bostedsadresse.kommunenummer,
                            null,
                    ), null, null)
        }

        return PdlSoknadPersonResponse(
                errors = null,
                data = PdlSoknadHentPerson(
                        hentPerson = PdlSoknadPerson(
                                adressebeskyttelse = listOf(adressebeskyttelse),
                                bostedsadresse = listOf(bostedsadresse),
                                kontaktadresse = emptyList(),
                                oppholdsadresse = emptyList(),
                                familierelasjoner = familierelasjoner,
                                navn = listOf(navn),
                                sivilstand = listOf(sivilstand),
                                statsborgerskap = listOf(statsborgerskap)
                        )
                )
        )
    }

    fun getSoknadEktefelleResponseFor(ident: String): PdlSoknadEktefelleResponse {
        log.info("Henter PDL soknad data for (ektefelle) $ident")

        val pdlEktefelle = ektefelleMap[ident] ?: defaultEktefelle()

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

    fun leggTilPerson(personalia: Personalia) {
        if (personListe[personalia.fnr] != null && personListe[personalia.fnr]!!.locked) {
            throw RuntimeException("Ident ${personalia.fnr} is locked! Cannot update!")
        }
        personListe.put(personalia.fnr, personalia)
    }

    fun leggTilBarn(fnr: String, pdlBarn: PdlSoknadBarn) {
        barnMap[fnr] = pdlBarn
    }

    fun getPersonalia(ident: String): Personalia {
        return personListe.getOrElse(ident, { throw RuntimeException("Ident $ident not found!") })
    }

    fun getBarn(ident: String): PdlSoknadBarn {
        return barnMap.getOrElse(ident, { throw RuntimeException("Barn with ident $ident not found!") })
    }

    fun veryfyNotLocked(fnr: String) {
        val personalia = personListe[fnr]
        if (personalia != null && personalia.locked) {
            throw RuntimeException("Bruker er låst og skal ikke oppdateres!")
        }
    }

    fun getPersonListe(): List<Personalia> {
        val personListe = personListe.values.sortedBy { it.opprettetTidspunkt }
        return personListe
    }

    companion object {
        private val log by logger()

        private val defaultAdresse = PdlVegadresse("matrikkelId", "gateveien", 1, "A", null, "0101", "0301", "H101")
        private val annenAdresse = PdlVegadresse("matrikkelId2", "Karl Johans gate", 1, null, null, "0101", "0301", null)

        private val ektefelleSammeBosted = PdlSoknadEktefelle(
                adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                bostedsadresse = listOf(PdlBostedsadresse(null, defaultAdresse, null, null)),
                foedsel = listOf(PdlFoedsel(LocalDate.of(1955, 5, 5))),
                navn = listOf(PdlSoknadPersonNavn("LILLA", "", "EKTEFELLE"))
        )

        private val ektefelleAnnetBosted = PdlSoknadEktefelle(
                adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                bostedsadresse = listOf(PdlBostedsadresse(null, annenAdresse, null, null)),
                foedsel = listOf(PdlFoedsel(LocalDate.of(1966, 6, 6))),
                navn = listOf(PdlSoknadPersonNavn("GUL", "", "EKTEFELLE"))
        )

        private val ektefelleMedAdressebeskyttelse = PdlSoknadEktefelle(
                adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.FORTROLIG)),
                bostedsadresse = emptyList(),
                foedsel = emptyList(),
                navn = emptyList()
        )

        private fun defaultEktefelle() =
                PdlSoknadEktefelle(
                        adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                        bostedsadresse = listOf(PdlBostedsadresse(null, defaultAdresse, null, null)),
                        foedsel = listOf(PdlFoedsel(LocalDate.of(1956, 4, 3))),
                        navn = listOf(PdlSoknadPersonNavn("Ektefelle", "", "McEktefelle"))
                )

        private fun defaultBarn() =
                PdlSoknadBarn(
                        adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.UGRADERT)),
                        bostedsadresse = listOf(PdlBostedsadresse(null, defaultAdresse, null, null)),
                        folkeregisterpersonstatus = listOf(PdlFolkeregisterpersonstatus("bosatt")),
                        foedsel = listOf(PdlFoedsel(LocalDate.now().minusYears(10))),
                        navn = listOf(PdlSoknadPersonNavn("Kid", "", "McKid"))
                )

    }
}

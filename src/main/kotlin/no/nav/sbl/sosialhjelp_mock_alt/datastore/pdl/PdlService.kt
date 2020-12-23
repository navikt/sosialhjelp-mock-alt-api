package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl

import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Adressebeskyttelse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Gradering
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Kjoenn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlFoedselsdato
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlInnsynPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlKjoenn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaHentPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaPerson
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlModiaPersonResponse
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlPersonNavn
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.PdlTelefonnummer
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp_mock_alt.utils.fastFnr
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class PdlService() {
    companion object {
        val log by logger()
    }

    final val personListe: HashMap<String, Personalia> = HashMap()

    init {
        personListe.put(fastFnr, Personalia(fnr = fastFnr)
                .withNavn("Standard", "", "Standarsen")
                .withOpprettetTidspunkt(0)
                .locked())
        val hemmeligBruker = Personalia()
                .withNavn("Hemmelig", "", "Adressesen")
                .withAdressebeskyttelse(Gradering.STRENGT_FORTROLIG)
                .withOpprettetTidspunkt(1)
                .locked()
        personListe.put(hemmeligBruker.fnr, hemmeligBruker)
        val svenskBruker = Personalia()
                .withNavn("Svenske", "", "Svenskersen")
                .withStarsborgerskap("SWE")
                .withOpprettetTidspunkt(2)
                .locked()
        personListe.put(svenskBruker.fnr, svenskBruker)
    }

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

    fun leggTilPerson(personalia: Personalia) {
        personListe.put(personalia.fnr, personalia)
    }

    fun getPersonalia(ident: String): Personalia {
        return personListe.getOrElse(ident, { throw RuntimeException("Ident $ident not found!") })
    }

    fun getPersonListe(): List<Personalia> {
        val personListe = personListe.values.sortedBy { it.opprettetTidspunkt }
        return personListe
    }
}

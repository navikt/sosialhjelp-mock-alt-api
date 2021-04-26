package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import org.joda.time.DateTime

data class Personalia(
    val fnr: String = genererTilfeldigPersonnummer(),
    val navn: PdlPersonNavn = PdlPersonNavn(),
    var adressebeskyttelse: Gradering = Gradering.UGRADERT,
    var sivilstand: String = "UOPPGITT",
    var ektefelle: String? = null,
    var forelderBarnRelasjon: List<ForelderBarnRelasjon> = emptyList(),
    var starsborgerskap: String = "NOR",
    var bostedsadresse: ForenkletBostedsadresse = ForenkletBostedsadresse("Hovedveien", 42, "0101", "0301"),
    var locked: Boolean = false,
    var opprettetTidspunkt: Long = DateTime.now().millis
) {
    fun withNavn(fornavn: String, mellomnavn: String, etternavn: String): Personalia {
        navn.fornavn = fornavn
        navn.mellomnavn = mellomnavn
        navn.etternavn = etternavn
        return this
    }

    fun withAdressebeskyttelse(nyVerdi: Gradering): Personalia {
        adressebeskyttelse = nyVerdi
        return this
    }

    fun withSivilstand(nyVerdi: String): Personalia {
        sivilstand = nyVerdi
        return this
    }

    fun withEktefelle(nyVerdi: String): Personalia {
        ektefelle = nyVerdi
        return this
    }

    fun withStarsborgerskap(nyVerdi: String): Personalia {
        starsborgerskap = nyVerdi
        return this
    }

    fun locked(): Personalia {
        locked = true
        return this
    }

    fun withOpprettetTidspunkt(tidspunkt: Long): Personalia {
        opprettetTidspunkt = tidspunkt
        return this
    }

    fun withBostedsadresse(nyBostedsadresse: ForenkletBostedsadresse): Personalia {
        bostedsadresse = nyBostedsadresse
        return this
    }

    fun withForelderBarnRelasjon(barnFnr: String): Personalia {
        forelderBarnRelasjon = listOf(ForelderBarnRelasjon(barnFnr, "barn", "forelder"))
        return this
    }
}

data class ForelderBarnRelasjon(
    val ident: String = genererTilfeldigPersonnummer(),
    val rolle: String = "barn",
    val motrolle: String = "forelder",
)

data class ForenkletBostedsadresse(
    val adressenavn: String,
    val husnummer: Int,
    val postnummer: String,
    val kommunenummer: String
)

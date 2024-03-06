package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

import java.time.LocalDate
import no.nav.sbl.sosialhjelp_mock_alt.utils.genererTilfeldigPersonnummer
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomDate
import org.joda.time.DateTime

data class Personalia(
    val fnr: String = genererTilfeldigPersonnummer(),
    val navn: PdlPersonNavn = PdlPersonNavn(),
    var adressebeskyttelse: Gradering = Gradering.UGRADERT,
    var sivilstand: String = "UOPPGITT",
    var ektefelleType: String? = null,
    var ektefelleFnr: String? = null,
    var ektefelleFodselsdato: LocalDate = randomDate(),
    var forelderBarnRelasjon: List<ForelderBarnRelasjon> = emptyList(),
    var starsborgerskap: String = "NOR",
    var bostedsadresse: ForenkletBostedsadresse =
        ForenkletBostedsadresse(
            adressenavn = "Hovedveien",
            husnummer = 42,
            postnummer = "0101",
            kommunenummer = "0301"),
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

  fun withEktefelleType(nyVerdi: String): Personalia {
    ektefelleType = nyVerdi
    return this
  }

  fun withEktefelleFodselsDato(nyDato: LocalDate): Personalia {
    ektefelleFodselsdato = nyDato
    ektefelleFnr = genererTilfeldigPersonnummer(ektefelleFodselsdato)
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

  fun withForelderBarnRelasjon(fnrBarn: List<String>): Personalia {
    val nyForeldreBarnRelasjon = fnrBarn.map { ForelderBarnRelasjon(it, "barn", "forelder") }
    forelderBarnRelasjon = nyForeldreBarnRelasjon
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
    val husbokstav: String? = null,
    val postnummer: String,
    val kommunenummer: String
)

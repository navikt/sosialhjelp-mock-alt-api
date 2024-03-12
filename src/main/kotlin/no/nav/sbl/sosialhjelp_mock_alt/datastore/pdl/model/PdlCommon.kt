package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

import java.time.LocalDate
import java.time.LocalDateTime

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(val line: Int?, val column: Int?)

data class PdlErrorExtension(val code: String?, val classification: String)

data class Adressebeskyttelse(val gradering: Gradering)

enum class Gradering {
  STRENGT_FORTROLIG_UTLAND, // kode 6 (utland)
  STRENGT_FORTROLIG, // kode 6
  FORTROLIG, // kode 7
  UGRADERT
}

data class PdlPersonNavn(
    var fornavn: String = "",
    var mellomnavn: String = "",
    var etternavn: String = "",
) {
  override fun toString(): String {
    return ("$fornavn $mellomnavn $etternavn").replace("  ", " ")
  }
}

data class PdlKjoenn(val kjoenn: Kjoenn)

enum class Kjoenn {
  MANN,
  KVINNE,
  UKJENT
}

data class PdlFoedselsdato(val foedselsdato: String?)

data class PdlTelefonnummer(val landskode: String, val nummer: String, val prioritet: Int)

data class PdlBostedsadresse(
    val coAdressenavn: String?,
    val vegadresse: PdlVegadresse?,
    val matrikkeladresse: PdlMatrikkeladresse?,
    val ukjentBosted: PdlUkjentBosted?
)

data class PdlOppholdsadresse(
    val oppholdAnnetSted: String?,
    val coAdressenavn: String?,
    val vegadresse: PdlVegadresse?,
    val metadata: PdlMetadata?,
    val folkeregistermetadata: PdlFolkeregistermetadata?
)

data class PdlVegadresse(
    val matrikkelId: String?,
    val adressenavn: String?,
    val husnummer: Int?,
    val husbokstav: String?,
    val tilleggsnavn: String?,
    val postnummer: String?,
    val kommunenummer: String?,
    val bruksenhetsnummer: String?
)

data class PdlMatrikkeladresse(
    val matrikkelId: String?,
    val postnummer: String?,
    val tilleggsnavn: String?,
    val kommunenummer: String?,
    val bruksenhetsnummer: String?
)

data class PdlUkjentBosted(val bostedskommune: String?)

data class PdlForelderBarnRelasjon(
    val relatertPersonsIdent: String?,
    val relatertPersonsRolle: String?,
    val minRolleForPerson: String?
)

data class PdlFoedsel(val foedselsdato: LocalDate?)

data class PdlFolkeregisterpersonstatus(val status: String)

data class PdlSivilstand(
    val type: SivilstandType?,
    val relatertVedSivilstand: String?,
    val metadata: PdlMetadata = defaultMetadata(),
    val folkeregistermetadata: PdlFolkeregistermetadata = defaultFolkeregistermetadata(),
)

enum class SivilstandType {
  UOPPGITT,
  UGIFT,
  GIFT,
  ENKE_ELLER_ENKEMANN,
  SKILT,
  SEPARERT,
  PARTNER,
  SEPARERT_PARTNER,
  SKILT_PARTNER,
  GJENLEVENDE_PARTNER
}

data class PdlStatsborgerskap(val land: String?)

data class PdlMetadata(val master: String, val endringer: List<PdlEndring>)

data class PdlEndring(val kilde: String?, val registrert: LocalDateTime?, val type: String?)

data class PdlFolkeregistermetadata(val ajourholdstidspunkt: LocalDateTime?, val kilde: String?)

fun defaultMetadata() =
    PdlMetadata(
        "PDL",
        listOf(
            PdlEndring(
                kilde = "NAV", registrert = LocalDateTime.now().minusDays(7), type = "type")))

fun defaultFolkeregistermetadata() =
    PdlFolkeregistermetadata(
        ajourholdstidspunkt = LocalDateTime.now().minusDays(6), kilde = "kilde")

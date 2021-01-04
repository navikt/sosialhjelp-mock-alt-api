package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

data class PdlError(
        val message: String,
        val locations: List<PdlErrorLocation>,
        val path: List<String>?,
        val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
        val line: Int?,
        val column: Int?
)

data class PdlErrorExtension(
        val code: String?,
        val classification: String
)

data class Adressebeskyttelse(
        val gradering: Gradering
)

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
)

data class PdlKjoenn(
        val kjoenn: Kjoenn
)

enum class Kjoenn { MANN, KVINNE, UKJENT }

data class PdlFoedselsdato(
        val foedselsdato: String?
)

data class PdlTelefonnummer(
        val landskode: String,
        val nummer: String,
        val prioritet: Int
)

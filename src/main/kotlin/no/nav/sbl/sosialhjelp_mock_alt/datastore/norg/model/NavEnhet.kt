package no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.model

data class NavEnhet(
    val enhetId: Int,
    val navn: String,
    val enhetNr: String,
    val status: String,
    val antallRessurser: Int,
    val aktiveringsdato: String,
    val nedleggelsesdato: String?,
    val sosialeTjenester: String?,
    val type: String = "LOKAL"
)

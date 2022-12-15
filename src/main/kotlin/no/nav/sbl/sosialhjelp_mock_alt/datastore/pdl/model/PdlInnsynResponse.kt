package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

data class PdlInnsynPersonResponse(val errors: List<PdlError>?, val data: PdlInnsynHentPerson?)

data class PdlInnsynHentPerson(val hentPerson: PdlInnsynPerson?)

data class PdlInnsynPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val navn: List<PdlPersonNavn>
)

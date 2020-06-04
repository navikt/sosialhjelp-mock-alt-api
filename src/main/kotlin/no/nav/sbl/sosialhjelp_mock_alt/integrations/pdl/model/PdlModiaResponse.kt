package no.nav.sbl.sosialhjelp_mock_alt.integrations.pdl.model

data class PdlModiaPersonResponse(
        val errors: List<PdlError>?,
        val data: PdlModiaHentPerson
)

data class PdlModiaHentPerson(
        val hentPerson: PdlModiaPerson?
)

data class PdlModiaPerson(
        val navn: List<PdlPersonNavn>,
        val kjoenn: List<PdlKjoenn>,
        val foedsel: List<PdlFoedselsdato>,
        val telefonnummer: List<PdlTelefonnummer>
)


package no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model

data class PdlSoknadPersonResponse(
        val errors: List<PdlError>?,
        val data: PdlSoknadHentPerson?
)

data class PdlSoknadEktefelleResponse(
        val errors: List<PdlError>?,
        val data: PdlSoknadHentEktefelle?
)

data class PdlSoknadBarnResponse(
        val errors: List<PdlError>?,
        val data: PdlSoknadHentBarn?
)

data class PdlSoknadHentPerson(
        val hentPerson: PdlSoknadPerson?
)

data class PdlSoknadPerson(
        val adressebeskyttelse: List<Adressebeskyttelse>?,
        val bostedsadresse: List<PdlBostedsadresse>?,
        val oppholdsadresse: List<PdlOppholdsadresse>?,
        val kontaktadresse: List<PdlKontaktadresse>?,
        val familierelasjoner: List<PdlFamilierelasjon>?,
        val navn: List<PdlSoknadPersonNavn>?,
        val sivilstand: List<PdlSivilstand>?,
        val statsborgerskap: List<PdlStatsborgerskap>?,
)

data class PdlSoknadHentEktefelle(
        val hentPerson: PdlSoknadEktefelle?
)

data class PdlSoknadEktefelle(
        val adressebeskyttelse: List<Adressebeskyttelse>?,
        val bostedsadresse: List<PdlBostedsadresse>?,
        val foedsel: List<PdlFoedsel>?,
        val navn: List<PdlSoknadPersonNavn>?,
)

data class PdlSoknadHentBarn(
        val hentPerson: PdlSoknadBarn?
)

data class PdlSoknadBarn(
        val adressebeskyttelse: List<Adressebeskyttelse>?,
        val bostedsadresse: List<PdlBostedsadresse>?,
        val folkeregistepersonstatus: List<PdlFolkeregisterpersonstatus>?,
        val foedsel: List<PdlFoedsel>?,
        val navn: List<PdlSoknadPersonNavn>?,
)

data class PdlSoknadPersonNavn(
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
        val metadata: PdlMetadata?,
        val folkeregistermetadata: PdlFolkeregistermetadata?
)
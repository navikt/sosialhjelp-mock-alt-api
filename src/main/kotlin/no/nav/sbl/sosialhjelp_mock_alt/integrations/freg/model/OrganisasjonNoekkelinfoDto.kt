package no.nav.sbl.sosialhjelp_mock_alt.integrations.freg.model

class OrganisasjonNoekkelinfoDto(val navn:NavnDto, val organisasjonsnummer: String)

class NavnDto(
        val navnelinje1: String,
        val navnelinje2: String = "",
        val navnelinje3: String = "",
        val navnelinje4: String = "",
        val navnelinje5: String = "",
)

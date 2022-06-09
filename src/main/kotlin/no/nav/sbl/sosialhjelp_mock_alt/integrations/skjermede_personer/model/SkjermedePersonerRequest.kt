package no.nav.sbl.sosialhjelp_mock_alt.integrations.skjermede_personer.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SkjermedePersonerRequest(
    @JsonProperty("personident") val personIdent: String
)

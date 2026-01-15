package no.nav.sbl.sosialhjelp.mock.alt.integrations.skjermedepersoner.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SkjermedePersonerRequest(
    @param:JsonProperty("personident") val personIdent: String,
)

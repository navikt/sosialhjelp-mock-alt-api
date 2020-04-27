package no.nav.sbl.sosialhjelp_mock_alt.integrations.abac.model

import com.fasterxml.jackson.annotation.JsonProperty

//data class Attributes(
//        @JsonProperty("Attribute")
//        var attributes: MutableList<Attribute>
//)

data class Attribute(
        @JsonProperty("AttributeId")
        val attributeId: String,
        @JsonProperty("Value")
        val value: String
)
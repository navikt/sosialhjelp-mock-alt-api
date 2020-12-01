package no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class KodeverkDto @JsonCreator constructor(
        val betydninger: Map<String, List<BetydningDto>>
)

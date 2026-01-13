package no.nav.sbl.sosialhjelp.mock.alt.integrations.kodeverk.dto

import com.fasterxml.jackson.annotation.JsonCreator

class KodeverkDto
    @JsonCreator
    constructor(
        val betydninger: Map<String, List<BetydningDto>>,
    )

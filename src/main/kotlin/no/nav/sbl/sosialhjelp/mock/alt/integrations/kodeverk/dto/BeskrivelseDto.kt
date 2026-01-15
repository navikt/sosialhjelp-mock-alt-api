package no.nav.sbl.sosialhjelp.mock.alt.integrations.kodeverk.dto

import com.fasterxml.jackson.annotation.JsonCreator

class BeskrivelseDto
    @JsonCreator
    constructor(
        val term: String,
        val tekst: String,
    )

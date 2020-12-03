package no.nav.sbl.sosialhjelp_mock_alt.integrations.kodeverk.dto

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate

class BetydningDto @JsonCreator constructor(
        val gyldigFra: LocalDate,
        val gyldigTil: LocalDate,
        val beskrivelser: Map<String, BeskrivelseDto>
)

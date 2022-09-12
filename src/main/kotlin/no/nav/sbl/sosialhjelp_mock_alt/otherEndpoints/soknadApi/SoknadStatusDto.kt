package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import java.time.LocalDateTime

class SoknadStatusDto(
    val ident: String,
    val navEnhet: String,
    val innsendtDato: LocalDateTime
)

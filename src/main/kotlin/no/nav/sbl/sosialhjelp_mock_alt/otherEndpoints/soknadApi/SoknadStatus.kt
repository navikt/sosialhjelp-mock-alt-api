package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import java.time.LocalDateTime

class SoknadStatus(
    val ident: String,
    val enhetsnr: String,
    val innsendtDato: LocalDateTime
)

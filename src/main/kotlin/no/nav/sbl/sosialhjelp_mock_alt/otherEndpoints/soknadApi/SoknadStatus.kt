package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import java.time.LocalDateTime

class SoknadStatus(
    val digisosId: String,
    val enhetsnr: String,
    val innsendtDato: LocalDateTime
)

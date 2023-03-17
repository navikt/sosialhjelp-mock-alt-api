package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.soknadApi

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class SaksListeDto(
    val fiksDigisosId: String?,
    val soknadTittel: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") val sistOppdatert: Date,
    val kilde: String,
    val url: String?
)

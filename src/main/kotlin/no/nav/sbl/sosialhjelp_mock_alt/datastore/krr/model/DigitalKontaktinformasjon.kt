package no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.model

data class DigitalKontaktinformasjon(
    val personident: String,
    val aktiv: Boolean,
    val kanVarsles: Boolean?,
    val reservert: Boolean?,
    val spraak: String?,
    val epostadresse: String?,
    val mobiltelefonnummer: String?,
    val sikkerDigitalPostkasse: SikkerDigitalPostkasse?,
)

data class SikkerDigitalPostkasse(
    val adresse: String,
    val leverandoerAdresse: String,
    val leverandoerSertifikat: String,
)

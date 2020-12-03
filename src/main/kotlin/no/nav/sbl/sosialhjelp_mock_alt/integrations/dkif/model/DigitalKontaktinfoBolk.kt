package no.nav.sbl.sosialhjelp_mock_alt.integrations.dkif.model

class DigitalKontaktinfoBolk(
        val kontaktinfo: Map<String, DigitalKontaktinfo>,
        val feil: Map<String, Feil>?
)

class DigitalKontaktinfo(val mobiltelefonnummer: String)

class Feil(val melding: String)

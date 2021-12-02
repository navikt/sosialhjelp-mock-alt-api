package no.nav.sbl.sosialhjelp_mock_alt.datastore.krr

import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.model.DigitalKontaktinformasjon
import org.springframework.stereotype.Service

@Service
class KrrService {
    private val krrKonfigursjoner = mutableMapOf<String, DigitalKontaktinformasjon>()

    fun hentKonfigurasjon(ident: String): DigitalKontaktinformasjon {
        krrKonfigursjoner[ident]?.let { return it }
        val defaultKRR = nyKontaktinformasjon(ident)
        krrKonfigursjoner[ident] = defaultKRR
        return defaultKRR
    }

    private fun nyKontaktinformasjon(
        ident: String,
        telefonnummer: String = "11112222"
    ) = DigitalKontaktinformasjon(
        ident,
        aktiv = true,
        kanVarsles = true,
        reservert = false,
        spraak = "no-nb",
        epostadresse = "epost@adresse.sen",
        mobiltelefonnummer = telefonnummer,
        sikkerDigitalPostkasse = null
    )

    fun setTelefonnummer(ident: String, telefonnummer: String) {
        val kontaktinformasjon = nyKontaktinformasjon(ident, telefonnummer = telefonnummer)
        krrKonfigursjoner[ident] = kontaktinformasjon
    }
}

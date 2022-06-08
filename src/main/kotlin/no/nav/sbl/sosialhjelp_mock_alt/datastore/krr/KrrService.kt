package no.nav.sbl.sosialhjelp_mock_alt.datastore.krr

import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.model.DigitalKontaktinformasjon
import org.springframework.stereotype.Service

@Service
class KrrService {
    private val krrKonfigursjoner = mutableMapOf<String, DigitalKontaktinformasjon>()

    fun hentKonfigurasjon(ident: String): DigitalKontaktinformasjon {
        krrKonfigursjoner[ident]?.let { return it }
        return oppdaterKonfigurasjon(ident, true) ?: throw RuntimeException("Problemer ved henting av KRR info")
    }

    fun oppdaterKonfigurasjon(
        ident: String,
        kanVarsles: Boolean,
        epostadresse: String = "epost@adresse.sen",
        telefonnummer: String = "11112222"
    ): DigitalKontaktinformasjon? {
        krrKonfigursjoner[ident] = nyKontaktinformasjon(ident, epostadresse, telefonnummer, kanVarsles)
        return krrKonfigursjoner[ident]
    }

    private fun nyKontaktinformasjon(
        ident: String,
        epostadresse: String?,
        telefonnummer: String?,
        kanVarsles: Boolean = true,
    ) = DigitalKontaktinformasjon(
        ident,
        aktiv = true,
        kanVarsles = true,
        reservert = !kanVarsles,
        spraak = "no-nb",
        epostadresse = epostadresse,
        mobiltelefonnummer = telefonnummer,
        sikkerDigitalPostkasse = null
    )

    fun setTelefonnummer(ident: String, telefonnummer: String) {
        val gammelKonfig = krrKonfigursjoner[ident]
        val kontaktinformasjon = nyKontaktinformasjon(
            ident,
            gammelKonfig?.epostadresse,
            telefonnummer,
            gammelKonfig?.kanVarsles ?: true
        )
        krrKonfigursjoner[ident] = kontaktinformasjon
    }
}

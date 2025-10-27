package no.nav.sbl.sosialhjelp_mock_alt.datastore.krr

import no.nav.sbl.sosialhjelp_mock_alt.datastore.krr.model.DigitalKontaktinformasjon
import org.springframework.stereotype.Service

@Service
class KrrService {
  private val krrKonfigursjoner = mutableMapOf<String, DigitalKontaktinformasjon>()

  fun hentKonfigurasjon(ident: String): DigitalKontaktinformasjon {
    krrKonfigursjoner[ident]?.let {
      return it
    }
    val defaultKontaktinformasjon =
        nyKontaktinformasjon(ident, "epost@adrsse.sen", "11112222", true)
    krrKonfigursjoner[ident] = defaultKontaktinformasjon
    return defaultKontaktinformasjon
  }

  fun oppdaterKonfigurasjon(
      ident: String,
      kanVarsles: Boolean,
      epost: String = "epost@adresse.sen",
      telefonnummer: String = "11112222",
  ) {
    krrKonfigursjoner[ident] = nyKontaktinformasjon(ident, epost, telefonnummer, kanVarsles)
  }

  private fun nyKontaktinformasjon(
      ident: String,
      epostadresse: String?,
      telefonnummer: String?,
      kanVarsles: Boolean = true,
  ) =
      DigitalKontaktinformasjon(
          ident,
          aktiv = true,
          kanVarsles = true,
          reservert = !kanVarsles,
          spraak = "no-nb",
          epostadresse = epostadresse,
          mobiltelefonnummer = telefonnummer,
          sikkerDigitalPostkasse = null,
      )
}

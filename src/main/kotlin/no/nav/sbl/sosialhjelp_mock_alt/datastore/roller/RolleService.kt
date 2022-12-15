package no.nav.sbl.sosialhjelp_mock_alt.datastore.roller

import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.model.AdminRolle
import no.nav.sbl.sosialhjelp_mock_alt.datastore.roller.model.AzureAdRoller
import org.springframework.stereotype.Service

@Service
class RolleService {
  private val rolleKonfigursjoner = mutableMapOf<String, AzureAdRoller>()

  fun hentKonfigurasjon(ident: String): List<AdminRolle> {
    rolleKonfigursjoner[ident]?.let {
      return it.roller
    }
    return emptyList()
  }

  fun leggTilKonfigurasjon(ident: String, roller: List<AdminRolle>) {
    rolleKonfigursjoner[ident] = AzureAdRoller(roller)
  }

  fun finnBrukerIDerForRolle(rolle: AdminRolle): List<String> {
    return rolleKonfigursjoner
        .filter { rolleKonfigursjoner[it.key]?.roller?.contains(rolle) ?: false }
        .map { it.key }
  }
}

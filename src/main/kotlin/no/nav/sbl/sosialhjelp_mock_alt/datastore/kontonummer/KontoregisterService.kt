package no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer

import no.nav.sbl.sosialhjelp_mock_alt.datastore.kontonummer.model.KontoDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Component

@Component
class KontoregisterService {

  private val kontoMap: HashMap<String, KontoDto> = HashMap()

  fun getKonto(ident: String): KontoDto? {
    log.info("Henter kontonummer for $ident")
    return kontoMap[ident]
  }

  fun putKonto(ident: String, konto: String) {
    log.info("Lagrer kontonummer $konto for ident: $ident")
    kontoMap[ident] = KontoDto(konto, null)
  }

  companion object {
    private val log by logger()
  }
}

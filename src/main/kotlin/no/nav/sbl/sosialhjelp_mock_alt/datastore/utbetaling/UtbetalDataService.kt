package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalDataDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class UtbetalDataService {

  private val utbetalDataListMap: HashMap<String, List<UtbetalDataDto>> = HashMap()
  private val autoGenerationSet: HashSet<String> = HashSet()

  fun getUtbetalingerFraNav(ident: String): List<UtbetalDataDto> {

    log.info("Henter utbetalinger for $ident")

    if (autoGenerationSet.contains(ident)) {
      return listOf(UtbetalDataDto())
    }
    return utbetalDataListMap[ident] ?: listOf(UtbetalDataDto())
  }

  fun putUtbetalingerFraNav(ident: String, utbetalinger: List<UtbetalDataDto>) {
    utbetalDataListMap[ident] = utbetalinger
  }

  fun enableAutoGenerationFor(fnr: String) {
    autoGenerationSet.add(fnr)
  }

  companion object {
    private val log by logger()
  }
}

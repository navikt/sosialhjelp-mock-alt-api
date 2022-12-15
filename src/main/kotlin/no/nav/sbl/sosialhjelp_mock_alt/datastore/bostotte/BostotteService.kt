package no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte

import java.time.LocalDate
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteStatus
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.SakerDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.UtbetalingerDto
import org.joda.time.DateTime
import org.springframework.stereotype.Service

@Service
class BostotteService {

  private val bostotteMap: HashMap<String, BostotteDto> = HashMap()
  private val autoGenerationSet: HashSet<String> = HashSet()

  fun putBostotte(fnr: String, bostotteDto: BostotteDto) {
    bostotteMap[fnr] = bostotteDto
  }

  fun getBostotte(fnr: String): BostotteDto {
    if (autoGenerationSet.contains(fnr)) {
      val sakDato = DateTime.now().minusDays(2)
      val utbetalingDato = LocalDate.now().minusDays(7)
      return BostotteDto(
          mutableListOf(
              SakerDto(
                  ar = sakDato.year,
                  mnd = sakDato.monthOfYear,
                  status = BostotteStatus.UNDER_BEHANDLING,
              )),
          mutableListOf(UtbetalingerDto(belop = 14000.0, utbetalingsdato = utbetalingDato)))
    }
    val bostotteDto = bostotteMap[fnr]
    if (bostotteDto != null) return bostotteDto
    return BostotteDto()
  }

  fun enableAutoGenerationFor(fnr: String) {
    autoGenerationSet.add(fnr)
  }
}

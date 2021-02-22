package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingsListeDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.joda.time.DateTime
import org.springframework.stereotype.Service

@Service
class UtbetalingService {

    final val utbetalingslisten: HashMap<String, UtbetalingsListeDto> = HashMap()
    final val autoGenerationSet: HashSet<String> = HashSet()

    fun putUtbetalingerFraNav(fnr: String, utbetaling: UtbetalingsListeDto) {
        utbetalingslisten[fnr] = utbetaling
    }

    fun getUtbetalingerFraNav(fnr: String): UtbetalingsListeDto {
        if (autoGenerationSet.contains(fnr)) {
            return UtbetalingsListeDto().add(UtbetalingDto(12000.0, DateTime.now().minusDays(14).toDate()))
        }
        return utbetalingslisten[fnr] ?: UtbetalingsListeDto()
    }

    fun enableAutoGenerationFor(fnr: String) {
        autoGenerationSet.add(fnr)
    }

    companion object {
        private val log by logger()
    }
}

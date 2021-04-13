package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class UtbetalingService {
    private final val utbetalingListMap: HashMap<String, List<UtbetalingDto>> = HashMap()

    fun getUtbetalingerFraNav(ident: String): List<UtbetalingDto> {
        log.info("Henter utbetalinger for $ident")
        return utbetalingListMap[ident] ?: emptyList()
    }

    fun putUtbetalingerFraNav(ident: String, utbetalinger: List<UtbetalingDto>) {
        utbetalingListMap[ident] = utbetalinger
    }

    companion object {
        private val log by logger()
    }

}

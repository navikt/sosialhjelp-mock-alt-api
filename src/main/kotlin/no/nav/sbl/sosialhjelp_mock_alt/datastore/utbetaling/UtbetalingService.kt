package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingsListeDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class UtbetalingService{

    final val utbetalingslisten: HashMap<String, UtbetalingsListeDto> = HashMap()

    fun putUtbetalingerFraNav(fnr: String, utbetaling: UtbetalingsListeDto) {
        utbetalingslisten[fnr] = utbetaling
    }

    fun getUtbetalingerFraNav(fnr: String): UtbetalingsListeDto {
        return utbetalingslisten[fnr] ?: UtbetalingsListeDto().add(UtbetalingDto())
    }

    companion object {
        private val log by logger()
    }
}

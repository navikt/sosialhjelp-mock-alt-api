package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalingDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UtbetalingService {
    private val utbetalingListMap: HashMap<String, List<UtbetalingDto>> = HashMap()
    private val autoGenerationSet: HashSet<String> = HashSet()

    fun getUtbetalingerFraNav(ident: String): List<UtbetalingDto> {
        log.info("Henter utbetalinger for $ident")
        if (autoGenerationSet.contains(ident)) {
            return listOf(UtbetalingDto(netto = 12000.0, utbetalingsdato = LocalDate.now().minusDays(14)))
        }
        return utbetalingListMap[ident] ?: listOf(UtbetalingDto())
    }

    fun putUtbetalingerFraNav(ident: String, utbetalinger: List<UtbetalingDto>) {
        utbetalingListMap[ident] = utbetalinger
    }

    fun enableAutoGenerationFor(fnr: String) {
        autoGenerationSet.add(fnr)
    }

    companion object {
        private val log by logger()
    }
}

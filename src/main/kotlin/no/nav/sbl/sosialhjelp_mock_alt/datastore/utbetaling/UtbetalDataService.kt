package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.UtbetalDataDto
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

//    private val utbetalingListMap: HashMap<String, List<UtbetalingDto>> = HashMap()
//    private val autoGenerationSet: HashSet<String> = HashSet()
//
//    fun getUtbetalingerFraNav(ident: String): List<UtbetalingDto> {
//        log.info("Henter utbetalinger for $ident")
//        if (autoGenerationSet.contains(ident)) {
//            return listOf(UtbetalingDto(netto = 12000.0, utbetalingsdato = LocalDate.now().minusDays(14)))
//        }
//        return utbetalingListMap[ident] ?: listOf(UtbetalingDto())
//    }
//
//    fun putUtbetalingerFraNav(ident: String, utbetalinger: List<UtbetalingDto>) {
//        utbetalingListMap[ident] = utbetalinger
//    }
//
//    fun enableAutoGenerationFor(fnr: String) {
//        autoGenerationSet.add(fnr)
//    }

    companion object {
        private val log by logger()
    }
}

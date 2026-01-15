package no.nav.sbl.sosialhjelp.mock.alt.datastore.utbetaling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp.mock.alt.datastore.utbetaling.model.UtbetalDataDto
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.stereotype.Service

@Service
class UtbetalDataService {
    private val utbetalDataListMap: HashMap<String, List<UtbetalDataDto>> = HashMap()
    private val autoGenerationSet: HashSet<String> = HashSet()

    // TODO Midlertidig utbetaling for standard standardsen
    init {
        leggTilUtbetalingerForStandardStandardsen()
    }

    fun putUtbetalingerFraNav(
        ident: String,
        utbetalinger: List<UtbetalDataDto>,
    ) {
        utbetalDataListMap[ident] = utbetalinger
    }

    fun getUtbetalingerFraNav(ident: String): List<UtbetalDataDto> {
        log.info("Henter utbetalinger for $ident")

        // TODO Eksluderer ident
        if (ident != "26504547549" && autoGenerationSet.contains(ident)) {
            return listOf(UtbetalDataDto())
        }
        return utbetalDataListMap[ident] ?: listOf(UtbetalDataDto())
    }

    fun enableAutoGenerationFor(fnr: String) {
        autoGenerationSet.add(fnr)
    }

    private fun leggTilUtbetalingerForStandardStandardsen() {
        utbetalDataListMap["26504547549"] = lesUtbetalingEksempel()
    }

    private fun lesUtbetalingEksempel(): List<UtbetalDataDto> {
        val string =
            this::class.java.classLoader
                .getResource("utbetaling/eksempel_utbetaling_fra_nav.json")!!
                .readText()
        return objectMapper.readValue(string)
    }

    companion object {
        private val log by logger()
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.Utbetaling
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class UtbetalDataService {

    private val utbetalingListMap: HashMap<String, List<Utbetaling>> = HashMap()
    private val autoGenerationSet: HashSet<String> = HashSet()

    fun getUtbetalingerFraNav(ident: String): List<Utbetaling> {
//        UtbetalingService.log.info("Henter utbetalinger for $ident")
//        if (autoGenerationSet.contains(ident)) {
        return listOf(
            Utbetaling(
                UtbetalData.Aktoer(UtbetalData.Aktoertype.PERSON, "123", "Testnavn"),
                "metode", "utbetalingsstatus", LocalDate.now(), LocalDate.now().plusDays(5L), LocalDate.now().plusDays(3L), BigDecimal(5500), "En melding", UtbetalData.Bankkonto("12345678911", "Brukskonto"), emptyList()
            )
        )
//        }
//        return utbetalingListMap[ident] ?: listOf(Utbetaling())
    }
}

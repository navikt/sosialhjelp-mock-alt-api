package no.nav.sbl.sosialhjelp.mock.alt.datastore.utbetaling

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UtbetalDataServiceTest {
    @Autowired
    private lateinit var utbetalDataService: UtbetalDataService

    @Test
    fun testGetUtbetalingerForStandardStandardsen() {
        val utbetalinger = utbetalDataService.getUtbetalingerFraNav("26504547549")
        assert(utbetalinger.isNotEmpty()) { "Utbetalinger should not be empty for standard standardsen" }
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sbl.sosialhjelp_mock_alt.datastore.utbetaling.model.UtbetalData.Utbetaling
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
class UtbetalDataService {

    fun getUtbetalingerFraNav(ident: String): List<Utbetaling> {
        val utbetaling = getUtbetalingFromJsonFile("inntekt/navutbetalinger/sokos-utbetaltdata-ekstern-response.json")
        return listOf(utbetaling)
    }

    private fun getUtbetalingFromJsonFile(file: String): Utbetaling {
        val resourceAsStream = ClassLoader.getSystemResourceAsStream(file)
        assert(resourceAsStream != null)
        val jsonString = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
        val mapper = jacksonObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        return mapper.readValue<Utbetaling>(jsonString)
    }
}

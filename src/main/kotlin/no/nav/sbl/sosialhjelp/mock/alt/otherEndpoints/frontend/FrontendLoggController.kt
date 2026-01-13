package no.nav.sbl.sosialhjelp.mock.alt.otherEndpoints.frontend

import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Selv om logging via endepunkter er noe vi er på vei vekk fra, var det behov for litt enkel
 * logging for å verifisere enkelte saker og ting. Derav denne svært enkle kontrolleren.
 */
@RestController
@RequestMapping("/mock-alt/v2/logg/")
class FrontendLoggController {
    companion object {
        private val log by logger()
    }

    data class LogEntryRequest(
        val message: String,
    )

    @PostMapping("/warn")
    fun logWarning(
        @RequestBody entry: LogEntryRequest,
    ) = log.warn(entry.message)

    @PostMapping("/info")
    fun logInfo(
        @RequestBody entry: LogEntryRequest,
    ) = log.info(entry.message)

    @PostMapping("/error")
    fun logError(
        @RequestBody entry: LogEntryRequest,
    ) = log.error(entry.message)
}

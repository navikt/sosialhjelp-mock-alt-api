package no.nav.sbl.sosialhjelp_mock_alt

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.RuntimeException

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    companion object {
        private val log by logger()
    }

    @ExceptionHandler(Throwable::class)
    fun handleAll(e: Throwable): ResponseEntity<String> {
        log.error(e.message, e)
        return ResponseEntity(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(KonfigurertFeil::class)
    fun handleKonfigurertFeil(e: KonfigurertFeil): ResponseEntity<String> {
        log.warn("Sender feilkode: ${e.feilkode} og melding: ${e.message}")
        return ResponseEntity.status(e.feilkode).body(e.message)
    }
}

class KonfigurertFeil(val feilkode: Int, message: String) : RuntimeException(message)

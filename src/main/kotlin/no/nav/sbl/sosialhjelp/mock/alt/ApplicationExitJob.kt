package no.nav.sbl.sosialhjelp.mock.alt

import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class ApplicationExitJob {
    val log by logger()

    @Scheduled(cron = "0 0 1 * * *")
    fun exitApplication() {
        log.info("Avslutter applikasjonen for å resette data")
        exitProcess(0)
    }
}

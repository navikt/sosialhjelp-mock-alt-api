package no.nav.sbl.sosialhjelp_mock_alt

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class NightlyCrasher {

    @Scheduled(cron = "30 13 * * *")
    fun nightlyRestart() {
        exitProcess(0)
    }
}

package no.nav.sbl.sosialhjelp_mock_alt

import kotlin.system.exitProcess
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NightlyCrasher {

  @Scheduled(cron = "45 13 * * *")
  fun nightlyRestart() {
    exitProcess(0)
  }
}

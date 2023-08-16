package no.nav.sbl.sosialhjelp_mock_alt

import kotlin.system.exitProcess
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NightlyCrasher {
  val log by logger()

  @Scheduled(cron = "0 14 * * * *")
  fun nightlyRestart() {
    log.info("Kræsjer appen for restart")
    exitProcess(0)
  }
}

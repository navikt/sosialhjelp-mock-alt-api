package no.nav.sbl.sosialhjelp_mock_alt

import kotlin.system.exitProcess
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ApplicationExitJob {
  val log by logger()

  @Scheduled(cron = "0 1 * * * *")
  fun exitApplication() {
    log.info("Avslutter applikasjonen for å resette data")
    exitProcess(0)
  }
}

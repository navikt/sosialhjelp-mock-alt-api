package no.nav.sbl.sosialhjelp_mock_alt.config

import no.ks.fiks.io.client.konfigurasjon.FiksIOKonfigurasjon
import no.ks.fiks.io.client.konfigurasjon.KontoKonfigurasjon
import no.ks.fiks.io.client.konfigurasjon.VirksomhetssertifikatKonfigurasjon
import no.ks.fiks.io.client.model.KontoId
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Configuration
class FiksIOConfig {
  @Bean
  @Profile("digisos-ekstern")
  fun fiksIOTestConfig(): FiksIOKonfigurasjon = FiksIOKonfigurasjon.defaultTestConfiguration(
    "abc",
    UUID.randomUUID(),
    "abc",
    KontoKonfigurasjon.builder().kontoId(
      KontoId(
        UUID.randomUUID()
      )
    ).build(),
    VirksomhetssertifikatKonfigurasjon.builder().build()
  )
}

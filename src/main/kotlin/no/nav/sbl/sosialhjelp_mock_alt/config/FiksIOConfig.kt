package no.nav.sbl.sosialhjelp_mock_alt.config

import java.nio.file.Files
import java.security.KeyFactory
import java.security.KeyStore
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID
import kotlin.io.path.Path
import no.ks.fiks.io.client.konfigurasjon.FiksIOKonfigurasjon
import no.ks.fiks.io.client.konfigurasjon.KontoKonfigurasjon
import no.ks.fiks.io.client.konfigurasjon.VirksomhetssertifikatKonfigurasjon
import no.ks.fiks.io.client.model.KontoId
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("digisos-ekstern")
class FiksIOConfig(
    private val kontoKonfig: KontoKonfigurasjon,
    @Value("\${fiks-io.integrasjonspassord}") private val integrasjonspassord: String,
    @Value("\${fiks-io.integrasjonsid}") private val integrasjonId: String,
    @Value("\${MASKINPORTEN_CLIENT_ID}") private val maskinportenClientId: String,
) {
  private val virksomhetssertifikatKonfigurasjon =
      VirksomhetssertifikatKonfigurasjon.builder()
          .keyStore(KeyStore.getInstance("pkcs12"))
          .keyStorePassword("bogus")
          .keyAlias("bogus")
          .keyPassword("bogus")
          .build()

  @Bean
  @Profile("digisos-ekstern")
  fun fiksIOTestConfig(): FiksIOKonfigurasjon =
      FiksIOKonfigurasjon.defaultTestConfiguration(
          maskinportenClientId,
          UUID.fromString(integrasjonId),
          integrasjonspassord,
          kontoKonfig,
          virksomhetssertifikatKonfigurasjon,
      )
}

@Configuration
@Profile("digisos-ekstern")
class FiksIoKontoConfig(
    @Value("\${fiks-io.private-key-path}") private val privateKeyPath: String,
    @Value("\${fiks-io.kontoId}") private val kontoId: String,
) {
  @Bean
  fun kontoKonfigurasjon(): KontoKonfigurasjon {
    val kontoId = KontoId(UUID.fromString(kontoId))
    val key = Files.readAllBytes(Path(privateKeyPath))
    val keySpec = PKCS8EncodedKeySpec(key)
    val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    return KontoKonfigurasjon.builder().kontoId(kontoId).privatNokkel(privateKey).build()
  }
}

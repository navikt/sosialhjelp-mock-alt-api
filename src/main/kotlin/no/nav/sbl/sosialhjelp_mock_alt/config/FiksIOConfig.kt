package no.nav.sbl.sosialhjelp_mock_alt.config

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import java.io.IOException
import java.nio.file.Files
import java.security.KeyFactory
import java.security.KeyStore
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID
import java.util.zip.CRC32C
import java.util.zip.Checksum
import kotlin.io.path.Path
import kotlinx.coroutines.runBlocking
import no.ks.fiks.io.client.FiksIOKlient
import no.ks.fiks.io.client.FiksIOKlientFactory
import no.ks.fiks.io.client.konfigurasjon.FiksIOKonfigurasjon
import no.ks.fiks.io.client.konfigurasjon.KontoKonfigurasjon
import no.ks.fiks.io.client.konfigurasjon.VirksomhetssertifikatKonfigurasjon
import no.ks.fiks.io.client.model.KontoId
import no.nav.sbl.sosialhjelp_mock_alt.maskinporten.MaskinportenClient
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("digisos-ekstern")
class FiksIOConfig(
    private val kontoKonfig: KontoKonfigurasjon,
    private val virksomhetssertifikatConfig: VirksomhetssertifikatKonfigurasjon,
    @Value("\${fiks-io.integrasjonspassord}") private val integrasjonspassord: String,
    @Value("\${fiks-io.integrasjonsid}") private val integrasjonId: String,
    @Value("\${MASKINPORTEN_CLIENT_ID}") private val maskinportenClientId: String,
) {
  @Bean
  fun fiksIOTestConfig(): FiksIOKonfigurasjon =
      FiksIOKonfigurasjon.defaultTestConfiguration(
          maskinportenClientId,
          UUID.fromString(integrasjonId),
          integrasjonspassord,
          kontoKonfig,
          virksomhetssertifikatConfig,
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

@Configuration
@Profile("digisos-ekstern")
class FiksIoKlientConfig(
    private val fiksIOKonfigurasjon: FiksIOKonfigurasjon,
    private val maskinportenClient: MaskinportenClient,
) {
  private val log by logger()

  @Bean
  fun fiksIOKlient(): FiksIOKlient {
    val fiksIOKlientFactory =
        FiksIOKlientFactory(fiksIOKonfigurasjon).apply {
          setMaskinportenAccessTokenSupplier {
            log.info("Henter maskinporten token for fiks io (klage)")
            runBlocking { maskinportenClient.getToken() }
          }
        }

    return fiksIOKlientFactory
        .runCatching { build() }
        .onFailure { log.error("Fikk ikke satt opp fiks IO-klient", it) }
        .getOrThrow()
  }
}

data class DigisosKeyStoreCredentials(val alias: String, val password: String, val type: String)

@Configuration
@Profile("digisos-ekstern")
class FiksIoVirksomhetssertifikatConfig(
    @Value("\${fiks-io.virksomhetssertifikat.passwordProjectId}")
    private val passwordProjectId: String,
    @Value("\${fiks-io.virksomhetssertifikat.passwordSecretId}")
    private val passwordSecretId: String,
    @Value("\${fiks-io.virksomhetssertifikat.passwordSecretVersion}")
    private val passwordSecretVersion: String,
    @Value("\${fiks-io.virksomhetssertifikat.projectId}") private val projectId: String,
    @Value("\${fiks-io.virksomhetssertifikat.secretId}") private val secretId: String,
    @Value("\${fiks-io.virksomhetssertifikat.secretVersion}") private val versionId: String,
) {

  private val log by logger()

  @Bean
  fun virksomhetssertifikatConfig(): VirksomhetssertifikatKonfigurasjon {
    val (sertifikat, password) =
        SecretManagerServiceClient.create().use { client ->
          val passwordResponse =
              client.accessSecretVersionResponse(
                  passwordProjectId, passwordSecretId, passwordSecretVersion)
          val certificateResponse =
              client.accessSecretVersionResponse(projectId, secretId, versionId)

          Pair(certificateResponse.payload, passwordResponse.payload)
        }
    val passwordThingy =
        objectMapper.readValue<DigisosKeyStoreCredentials>(password.data.toByteArray())
    val keyStore = KeyStore.getInstance("jceks")
    keyStore.load(sertifikat.data.newInput(), passwordThingy.password.toCharArray())
    return VirksomhetssertifikatKonfigurasjon.builder()
        .keyStore(keyStore)
        .keyStorePassword(passwordThingy.password)
        .keyAlias(passwordThingy.alias)
        .keyPassword(passwordThingy.password)
        .build()
  }

  private fun SecretManagerServiceClient.accessSecretVersionResponse(
      projectId: String,
      secretId: String,
      secretVersion: String
  ): AccessSecretVersionResponse {
    val secretVersionName = SecretVersionName.of(projectId, secretId, secretVersion)

    val response = accessSecretVersion(secretVersionName)

    val data = response.payload.data.toByteArray()
    val checksum: Checksum = CRC32C()
    checksum.update(data, 0, data.size)
    if (response.payload.dataCrc32C != checksum.value) {
      log.error("Data corruption detected.")
      throw IOException("Data corruption detected.")
    }
    return response
  }
}

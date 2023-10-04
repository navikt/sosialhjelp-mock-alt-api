package no.nav.sbl.sosialhjelp_mock_alt.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelp_mock_alt.maskinporten.MaskinportenClient
import no.nav.sbl.sosialhjelp_mock_alt.maskinporten.MaskinportenProperties
import no.nav.sbl.sosialhjelp_mock_alt.maskinporten.WellKnown
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Configuration
@Profile("digisos-ekstern")
class MaskinportenClientConfig(
    @Value("\${maskinporten_clientid}") clientId: String,
    @Value("\${maskinporten_scopes}") scopes: String,
    @Value("\${maskinporten_well_known_url}") private val wellKnownUrl: String,
    @Value("\${maskinporten_client_jwk}") clientJwk: String,
    webClientBuilder: WebClient.Builder,
) {

  private val log by logger()

  @Bean
  fun maskinportenClient(): MaskinportenClient =
      MaskinportenClient(maskinportenWebClient, maskinportenProperties, wellknown)

  protected val maskinportenWebClient: WebClient =
      webClientBuilder
          .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
          }
          .build()

  protected val maskinportenProperties =
      MaskinportenProperties(
          clientId = clientId, clientJwk = clientJwk, scope = scopes, wellKnownUrl = wellKnownUrl)

  protected val wellknown: WellKnown
    get() =
        runBlocking(Dispatchers.IO) {
          maskinportenWebClient
              .get()
              .uri(wellKnownUrl)
              .retrieve()
              .awaitBodyOrNull<WellKnown>()
              .also { log.info("Hentet WellKnown for Maskinporten.") }
              ?: throw RuntimeException("Feil ved henting av WellKnown for Maskinporten")
        }
}

package no.nav.sbl.sosialhjelp_mock_alt.maskinporten

import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

data class MaskinportenResponse(val access_token: String)

class MaskinportenClient(
    private val maskinportenWebClient: WebClient,
    maskinportenProperties: MaskinportenProperties,
    private val wellKnown: WellKnown,
) {
  private var cachedToken: SignedJWT? = null

  private val tokenGenerator =
      MaskinportenGrantTokenGenerator(maskinportenProperties, wellKnown.issuer)

  suspend fun getToken(): String {
    return getTokenFraCache() ?: getTokenFraMaskinporten()
  }

  private fun getTokenFraCache(): String? {
    return cachedToken?.takeUnless { isExpired(it) }?.parsedString
  }

  private suspend fun getTokenFraMaskinporten(): String =
      withContext(Dispatchers.IO) {
        val response =
            maskinportenWebClient
                .post()
                .uri(wellKnown.token_endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .awaitBodyOrNull<MaskinportenResponse>()
                .also { log.info("Hentet token fra Maskinporten") }
                ?: throw RuntimeException("Noe feilet ved henting av token fra Maskinporten")

        response.access_token.also { cachedToken = SignedJWT.parse(it) }
      }

  private val params: MultiValueMap<String, String>
    get() =
        LinkedMultiValueMap<String, String>().apply {
          add("grant_type", GRANT_TYPE)
          add("assertion", tokenGenerator.getJwt())
        }

  companion object {
    private val log by logger()

    private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
    private const val TJUE_SEKUNDER: Long = 20

    private fun isExpired(jwt: SignedJWT): Boolean {
      return jwt.jwtClaimsSet
          ?.expirationTime
          ?.toLocalDateTime
          ?.minusSeconds(TJUE_SEKUNDER)
          ?.isBefore(LocalDateTime.now())
          ?: true
    }

    private val Date.toLocalDateTime: LocalDateTime?
      get() = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime()
  }
}

data class WellKnown(
    val issuer: String,
    val token_endpoint: String,
)

data class MaskinportenProperties(
    val clientId: String,
    val clientJwk: String,
    val scope: String,
    val wellKnownUrl: String,
)

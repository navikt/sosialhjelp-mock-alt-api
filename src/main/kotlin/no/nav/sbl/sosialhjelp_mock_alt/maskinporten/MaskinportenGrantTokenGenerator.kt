package no.nav.sbl.sosialhjelp_mock_alt.maskinporten

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.Date

class MaskinportenGrantTokenGenerator(
    private val maskinportenProperties: MaskinportenProperties,
    private val issuer: String,
) {

  /**
   * Generer privateRsaKey hvis maskinporten_clientJwk == generateRSA, og appen ikke kjører i prod.
   * Dvs at rsaKey genereres i ved lokal kjøring, i test eller mot mock-alt.
   */
  private val privateRsaKey = RSAKey.parse(maskinportenProperties.clientJwk)

  fun getJwt(): String {
    return SignedJWT(signatureHeader, createJwtClaimSet(issuer))
        .apply { sign(RSASSASigner(privateRsaKey.toPrivateKey())) }
        .serialize()
  }

  private fun createJwtClaimSet(audience: String): JWTClaimsSet {
    val now: Instant = Instant.now()
    return JWTClaimsSet.Builder()
        .audience(audience)
        .issuer(maskinportenProperties.clientId)
        .claim(SCOPE_CLAIM, maskinportenProperties.scope)
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plusSeconds(120))) // 120s eller noe annet?
        .build()
  }

  private val signatureHeader
    get() =
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(privateRsaKey.keyID)
            .type(JOSEObjectType.JWT)
            .build()

  companion object {
    private const val SCOPE_CLAIM = "scope"
  }
}

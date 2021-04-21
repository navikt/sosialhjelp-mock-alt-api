package no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdPortenAccessTokenResponse(
    @JsonProperty(value = "access_token", required = true) val accessToken: String,
    @JsonProperty(value = "expires_in", required = true) val expiresIn: Int,
    @JsonProperty(value = "scope", required = true) val scope: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class IdPortenOidcConfiguration(
    @JsonProperty(value = "issuer", required = true) val issuer: String,
    @JsonProperty(value = "token_endpoint", required = true) val tokenEndpoint: String,
    @JsonProperty(value = "jwks_uri", required = false) val jwksURI: String?
)

// inline class AccessToken(val token: String) {
//    override fun toString(): String = token
// }
//
// inline class Jws(val token: String) {
//    override fun toString(): String = token
// }

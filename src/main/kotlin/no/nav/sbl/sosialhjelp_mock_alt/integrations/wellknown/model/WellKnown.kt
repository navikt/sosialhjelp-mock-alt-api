package no.nav.sbl.sosialhjelp_mock_alt.integrations.wellknown.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WellKnown(
    @param:JsonProperty(value = "issuer", required = true) val issuer: String,
    @param:JsonProperty(value = "token_endpoint", required = true) val tokenEndpoint: String,
    @param:JsonProperty(value = "jwks_uri", required = false) val jwksURI: String?
)

data class TokenResponse(
    @param:JsonProperty("access_token") val accessToken: String,
    @param:JsonProperty("issued_token_type") val issuedTokenType: String,
    @param:JsonProperty("token_type") val tokenType: String,
    @param:JsonProperty("expires_in") val expiresIn: Int
)

data class AzuredingsResponse(
    @param:JsonProperty("token_type") val tokenType: String,
    @param:JsonProperty("scope") val scope: String,
    @param:JsonProperty("expires_in") val expiresIn: Int,
    @param:JsonProperty("ext_expires_in") val extExpiresIn: Int,
    @param:JsonProperty("access_token") val accessToken: String,
    @param:JsonProperty("refresh_token") val refreshToken: String = "",
)

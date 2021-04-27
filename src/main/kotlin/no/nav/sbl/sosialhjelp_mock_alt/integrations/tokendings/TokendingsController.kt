package no.nav.sbl.sosialhjelp_mock_alt.integrations.tokendings

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.WellKnown
import no.nav.sbl.sosialhjelp_mock_alt.integrations.sts.StsController
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TokendingsController(
    @Value("\${host_address}") private val host_address: String,
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/tokendings/metadata")
    fun getMockAltMetadate(): WellKnown {
        val config = WellKnown(
            issuer = "digisos-mock-alt",
            tokenEndpoint = "${host_address}sosialhjelp/mock-alt-api/tokendings/token",
            jwksURI = "${host_address}sosialhjelp/mock-alt-api/local/jwks",
        )
        StsController.log.info("Henter tokendings konfigurasjon: $config")
        return config
    }

    @PostMapping("/tokendings/token", produces = ["application/json;charset=UTF-8"])
    fun exchangeToken(
        @RequestBody body: String,
    ): TokendingsTokenResponse {
        val typeRef = object : TypeReference<HashMap<String, String>>() {}
        val params = objectMapper.readValue(body, typeRef)
        log.info("Utveksler token: audience: ${params["audience"]}\n")
        return TokendingsTokenResponse(params["subject_token"]!!, "JWT", "JWT", 60)
    }
}

data class TokendingsTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("issued_token_type") val issuedTokenType: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Int
)

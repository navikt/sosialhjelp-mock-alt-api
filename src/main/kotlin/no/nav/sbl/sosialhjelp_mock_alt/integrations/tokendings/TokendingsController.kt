package no.nav.sbl.sosialhjelp_mock_alt.integrations.tokendings

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TokendingsController {
    companion object {
        private val log by logger()
    }

    @PostMapping("/tokendings", produces = ["application/json;charset=UTF-8"])
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

package no.nav.sbl.sosialhjelp_mock_alt.integrations.wellknown

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import no.nav.sbl.sosialhjelp_mock_alt.integrations.wellknown.model.AzuredingsResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.wellknown.model.TokenResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.wellknown.model.WellKnown
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WellKnownController(
    @Value("\${host_address}") private val hostAddress: String,
    private val proxyAwareResourceRetriever: ProxyAwareResourceRetriever,
    private val mockOAuth2Server: MockOAuth2Server
) {

    @GetMapping("/well-known/{issuer}")
    fun getMockAltMetadata(
        @PathVariable(value = "issuer") issuer: String
    ): WellKnown {
        val wellknown = WellKnown(
            issuer = mockOAuth2Server.issuerUrl(issuer).toString(),
            tokenEndpoint = "${hostAddress}sosialhjelp/mock-alt-api/token/$issuer",
            jwksURI = "${hostAddress}sosialhjelp/mock-alt-api/jwks/$issuer"
        )
        log.info("Metadata for issuer=$issuer: \n$wellknown")
        return wellknown
    }

    @GetMapping("/azure-well-known/{issuer}")
    fun getAzureMetadata(
        @PathVariable(value = "issuer") issuer: String
    ): WellKnown {
        val wellknown = WellKnown(
            issuer = mockOAuth2Server.issuerUrl(issuer).toString(),
            tokenEndpoint = "${hostAddress}sosialhjelp/mock-alt-api/azuretoken/$issuer",
            jwksURI = "${hostAddress}sosialhjelp/mock-alt-api/jwks/$issuer"
        )
        log.info("Metadata for issuer=$issuer: \n$wellknown")
        return wellknown
    }

    @GetMapping("/jwks/{issuer}")
    fun getMockAltJwks(
        @PathVariable(value = "issuer") issuer: String
    ): String {
        val jwksUrl = mockOAuth2Server.jwksUrl(issuer)
        val data = proxyAwareResourceRetriever.retrieveResource(jwksUrl.toUrl())
        log.info("Henter jwks for issuer=$issuer")
        return data.content
    }

    @PostMapping("/token/{issuer}", produces = ["application/json;charset=UTF-8"])
    fun exchangeToken(
        @RequestBody body: String,
    ): TokenResponse {
        val typeRef = object : TypeReference<HashMap<String, String>>() {}
        val params = try {
            objectMapper.readValue(body, typeRef)
        } catch (e: JsonParseException) {
            spitFormParams(body)
        }
        log.info("Utveksler token: audience: ${params["audience"]}\n")
        return TokenResponse(params["subject_token"]!!, "JWT", "JWT", 60)
    }

    @PostMapping("/azuretoken/{issuer}", produces = ["application/json;charset=UTF-8"])
    fun exchangeAzuretoken(
        @RequestBody body: String,
    ): AzuredingsResponse {
        val formsMap: HashMap<String, String> = spitFormParams(body)
        log.info("Utveksler azure token: audience: ${formsMap["audience"]}\n")
        return AzuredingsResponse("JWT", formsMap["scope"]!!, 60, 60, formsMap["assertion"]!!)
    }

    private fun splitFormParams(body: String): HashMap<String, String> {
        val formsMap: HashMap<String, String> = hashMapOf()
        val split = body.split("&")
        split.forEach {
            val innerSplit = it.split("=")
            formsMap[innerSplit[0]] = innerSplit[1]
        }
        return formsMap
    }

    companion object {
        private val log by logger()
    }
}

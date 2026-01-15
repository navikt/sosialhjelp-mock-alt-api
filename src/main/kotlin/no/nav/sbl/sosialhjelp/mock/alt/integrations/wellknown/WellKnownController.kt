package no.nav.sbl.sosialhjelp.mock.alt.integrations.wellknown

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.nimbusds.jwt.SignedJWT
import no.nav.sbl.sosialhjelp.mock.alt.integrations.wellknown.model.AzuredingsResponse
import no.nav.sbl.sosialhjelp.mock.alt.integrations.wellknown.model.TokenResponse
import no.nav.sbl.sosialhjelp.mock.alt.integrations.wellknown.model.WellKnown
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WellKnownController(
    @param:Value("\${host_address}") private val hostAddress: String,
    private val proxyAwareResourceRetriever: ProxyAwareResourceRetriever,
    private val mockOAuth2Server: MockOAuth2Server,
) {
    @GetMapping("/well-known/{issuer}")
    fun getMockAltMetadata(
        @PathVariable(value = "issuer") issuer: String,
        @RequestParam host: String?,
    ): WellKnown {
        val baseUrl = host?.let { hostAddress(it) } ?: hostAddress

        val wellknown =
            WellKnown(
                issuer = mockOAuth2Server.issuerUrl(issuer).toString(),
                tokenEndpoint = "${baseUrl}sosialhjelp/mock-alt-api/token/$issuer",
                jwksURI = "${baseUrl}sosialhjelp/mock-alt-api/jwks/$issuer",
            )
        log.info("Metadata for issuer=$issuer: \n$wellknown")
        return wellknown
    }

    @GetMapping("/azure-well-known/{issuer}")
    fun getAzureMetadata(
        @PathVariable(value = "issuer") issuer: String,
        @RequestParam host: String?,
    ): WellKnown {
        val baseUrl = host?.let { hostAddress(it) } ?: hostAddress
        val wellknown =
            WellKnown(
                issuer = mockOAuth2Server.issuerUrl(issuer).toString(),
                tokenEndpoint = "${baseUrl}sosialhjelp/mock-alt-api/azuretoken/$issuer",
                jwksURI = "${baseUrl}sosialhjelp/mock-alt-api/jwks/$issuer",
            )
        log.info("Metadata for issuer=$issuer: \n$wellknown")
        return wellknown
    }

    @GetMapping("/jwks/{issuer}")
    fun getMockAltJwks(
        @PathVariable(value = "issuer") issuer: String,
    ): String {
        val jwksUrl = mockOAuth2Server.jwksUrl(issuer)
        val data = proxyAwareResourceRetriever.retrieveResource(jwksUrl.toUrl())
        log.info("Henter jwks for issuer=$issuer")
        return data.content
    }

    @PostMapping("/token/{issuer}", produces = ["application/json;charset=UTF-8"])
    fun exchangeToken(
        @RequestBody body: String,
        @PathVariable(value = "issuer") issuer: String,
    ): TokenResponse {
        val typeRef = object : TypeReference<HashMap<String, String>>() {}

        val params =
            try {
                objectMapper.readValue(body, typeRef)
            } catch (e: JsonParseException) {
                splitFormParams(body)
            }

        if (params.containsKey("assertion") && params.containsKey("grant_type")) {
            log.info("Utveksler token for $issuer")
            return TokenResponse(params["assertion"]!!, "JWT", "JWT", 60)
        }

        // Dette er egentlig et kall til texas, prøver å simulere her
        if (params.containsKey("identity_provider") && params.containsKey("target")) {
            val token =
                mockOAuth2Server.issueToken(
                    issuerId = issuer,
                    params["user_token"]?.let { SignedJWT.parse(it).jwtClaimsSet.subject }
                        ?: "12345678910",
                    "audience",
                    mapOf("acr" to params["target"]!!),
                    1000L,
                )
            return TokenResponse(token.serialize(), "Bearer", "Bearer", 60)
        }

        log.info("Utveksler token for $issuer: audience: ${params["audience"]}\n")
        val subjectToken = params["subject_token"]!!

        val newToken =
            mockOAuth2Server.issueToken(
                issuerId = issuer,
                subject = SignedJWT.parse(subjectToken).jwtClaimsSet.subject,
                audience = params["audience"],
                claims = mapOf("acr" to "idporten-loa-high"),
            )
        return TokenResponse(newToken.serialize(), "JWT", "JWT", 60)
    }

    data class AzureTokenRequest(
        val grant_type: String?,
        val scope: String?,
        val assertion: String?,
        val audience: String?,
    )

    @PostMapping("/azuretoken/{issuer}", produces = ["application/json;charset=UTF-8"])
    fun exchangeAzuretoken(
        @ModelAttribute formsMap: AzureTokenRequest,
        @PathVariable(value = "issuer") issuer: String,
    ): AzuredingsResponse {
        if (formsMap.grant_type == "client_credentials") {
            log.info("Utveklser azure token (client credentials flow), scope: ${formsMap.scope}")
            return AzuredingsResponse("JWT", formsMap.scope!!, 60, 60, "token")
        }
        log.info("Utveksler azure token: audience: ${formsMap.audience}\n")
        return AzuredingsResponse("JWT", formsMap.scope!!, 60, 60, formsMap.assertion!!)
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

    private fun hostAddress(hostFromQueryParam: String): String {
        if (hostAddress.contains("localhost")) {
            return "http://$hostFromQueryParam:8989/"
        }
        return hostAddress
    }

    companion object {
        private val log by logger()
    }
}

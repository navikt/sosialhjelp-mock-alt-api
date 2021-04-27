package no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten

import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.IdPortenAccessTokenResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.WellKnown
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IdPortenController(
    @Value("\${host_address}") private val host_address: String
) {
    companion object {
        private val log by logger()
    }

    @PostMapping("/idporten/idporten-oidc-provider/token")
    fun getToken(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): IdPortenAccessTokenResponse {
        val token = IdPortenAccessTokenResponse(
            accessToken = "",
            expiresIn = 999999,
            scope = "ks:fiks"
        )
        log.info("Henter token: $token")
        return token
    }

    @GetMapping("/idporten/idporten-oidc-provider/.well-known/openid-configuration")
    fun getConfig(@RequestParam parameters: MultiValueMap<String, String>): WellKnown {
        val config = WellKnown(
            issuer = "iss-localhost",
            tokenEndpoint = "${host_address}sosialhjelp/mock-alt-api/idporten/idporten-oidc-provider/token",
            jwksURI = "${host_address}sosialhjelp/mock-alt-api/local/jwks"
        )
        log.info("Henter konfigurasjon: $config")
        return config
    }
}

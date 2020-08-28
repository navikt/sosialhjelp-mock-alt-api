package no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten

import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.IdPortenAccessTokenResponse
import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.IdPortenOidcConfiguration
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IdPortenController(
        @Value("\${host_address}") private val host_address: String
) {
    companion object {
        val log by logger()
    }

    @RequestMapping("/idporten/idporten-oidc-provider/token")
    fun getToken(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): String {
        val token = IdPortenAccessTokenResponse(
                accessToken = "",
                expiresIn = 999999,
                scope = "ks:fiks"
        )
        log.info("Henter token: $token")
        return objectMapper.writeValueAsString(token)
    }

    @RequestMapping("/idporten/idporten-oidc-provider/.well-known/openid-configuration")
    fun getConfig(@RequestParam parameters: MultiValueMap<String, String>): String {
        val config = IdPortenOidcConfiguration(
                issuer = "digisos-mock-alt",
                tokenEndpoint = "${host_address}sosialhjelp/mock-alt-api/idporten/idporten-oidc-provider/token"
        )
        log.info("Henter konfigurasjon: $config")
        return objectMapper.writeValueAsString(config)
    }
}
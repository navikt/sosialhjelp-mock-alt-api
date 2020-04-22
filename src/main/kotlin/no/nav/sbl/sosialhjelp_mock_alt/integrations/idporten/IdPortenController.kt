package no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten

import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IdPortenController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/idporten/idporten-oidc-provider/token")
    fun getToken(@RequestParam parameters:MultiValueMap<String, String>): String {
        val token = "{\"access_token\": \"accessTokenString\", \"expires_in\": 999999, \"scope\": \"ks:fiks\"}"
        log.info("Henter token: $token")
        return token
    }

    @RequestMapping("/idporten/idporten-oidc-provider/.well-known/openid-configuration")
    fun getConfig(@RequestParam parameters:MultiValueMap<String, String>): String {
        val config = "{\"issuer\": \"digisos-mock-alt\", \"token_endpoint\":\"http://127.0.0.1:8989/idporten/idporten-oidc-provider/token\"}"
        log.info("Henter konfigurasjon: $config")
        return config
    }
}
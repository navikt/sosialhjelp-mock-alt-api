package no.nav.sbl.sosialhjelp_mock_alt.integrations.sts

import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.IdPortenOidcConfiguration
import no.nav.sbl.sosialhjelp_mock_alt.integrations.sts.model.STSResponse
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class StsController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/sts_token_endpoint_url/token")
    fun getToken(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): String {
        val token = STSResponse(
                access_token = "token",
                token_type = "type",
                expires_in = 999999
        )
        log.info("Henter token: $token")
        log.info("Request body: $body")
        return objectMapper.writeValueAsString(token)
    }

    @RequestMapping("/sts_token_endpoint_url/.well-known/openid-configuration")
    fun getConfig(@RequestParam parameters: MultiValueMap<String, String>): String {
        val config = IdPortenOidcConfiguration(
                issuer = "digisos-mock-alt",
                tokenEndpoint = "http://127.0.0.1:8989/sts_token_endpoint_url/token"
        )
        log.info("Henter konfigurasjon: $config")
        return objectMapper.writeValueAsString(config)
    }

    @RequestMapping("/sts/authorisation")
    fun getStsAuthorisation(@RequestParam parameters:MultiValueMap<String, String>): String {
        val config = HashMap<String, Any>()
        config.put("issuer", "https://digisos.labs.nais.io/")
        config.put("subject_types_supported", listOf("public", "pairwise"))
        config.put("jwks_uri", "https://digisos.labs.nais.io/")
        log.info("Henter authorisation: $config")
        return objectMapper.writeValueAsString(config)
    }

    @RequestMapping("/sts/kodeverk")
    fun getKodeverk(@RequestParam parameters:MultiValueMap<String, String>): String {
        val config = XMLEnkeltKodeverk()
        log.info("Henter kodeverk: $config")
        return objectMapper.writeValueAsString(config)
    }
}
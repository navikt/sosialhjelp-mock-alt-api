package no.nav.sbl.sosialhjelp_mock_alt.integrations.sts

import no.nav.sbl.sosialhjelp_mock_alt.integrations.idporten.model.IdPortenOidcConfiguration
import no.nav.sbl.sosialhjelp_mock_alt.integrations.sts.model.STSResponse
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
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
    fun getStsAuthorisation(@RequestParam parameters: MultiValueMap<String, String>): String {
        val config = HashMap<String, Any>()
        config.put("issuer", "https://digisos.labs.nais.io/")
        config.put("subject_types_supported", listOf("public", "pairwise"))
        config.put("jwks_uri", "https://digisos.labs.nais.io/")
        log.info("Henter authorisation: $config")
        return objectMapper.writeValueAsString(config)
    }

    @RequestMapping("/sts/kodeverk")
    fun getKodeverk(@RequestParam parameters: MultiValueMap<String, String>): String {
        val config = "<ns2:hentKodeverkResponse xmlns:ns2=\"http://nav.no/tjeneste/virksomhet/kodeverk/v2/\">\n" +
                "         <response>\n" +
                "            <kodeverk xsi:type=\"ns4:EnkeltKodeverk\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns4=\"http://nav.no/tjeneste/virksomhet/kodeverk/v2/informasjon\">\n" +
                "               <uri>http://nav.no/kodeverk/Kodeverk/Landkoder?v=1</uri>\n" +
                "               <navn>Landkoder</navn>\n" +
                "               <gyldighetsperiode>\n" +
                "                  <fom>1900-01-01</fom>\n" +
                "                  <tom>9999-12-31</tom>\n" +
                "               </gyldighetsperiode>\n" +
                "               <type>KODEVERK</type>\n" +
                "               <versjonsnummer>1</versjonsnummer>\n" +
                "               <versjoneringsdato>2013-10-15</versjoneringsdato>\n" +
                "               <kode>\n" +
                "                  <uri>http://nav.no/kodeverk/Kode/Landkoder/_3f_3f_3f_20?v=1</uri>\n" +
                "                  <navn>???</navn>\n" +
                "                  <gyldighetsperiode>\n" +
                "                     <fom>1900-01-01</fom>\n" +
                "                     <tom>9999-12-31</tom>\n" +
                "                  </gyldighetsperiode>\n" +
                "                  <term>\n" +
                "                     <uri>http://nav.no/kodeverk/Term/Landkoder/_3f_3f_3f_20/nb/UOPPGITT_2fUKJENT?v=1</uri>\n" +
                "                     <navn>UOPPGITT/UKJENT</navn>\n" +
                "                     <gyldighetsperiode>\n" +
                "                        <fom>1900-01-01</fom>\n" +
                "                        <tom>9999-12-31</tom>\n" +
                "                     </gyldighetsperiode>\n" +
                "                     <spraak>nb</spraak>\n" +
                "                  </term>\n" +
                "               </kode>\n" +
                "               <!-- ... -->\n" +
                "            </kodeverk>\n" +
                "         </response>\n" +
                "      </ns2:hentKodeverkResponse>"
        log.info("Henter kodeverk: $config")
        return config
//        return objectMapper.writeValueAsString(config)
    }

    @PostMapping("/sts/SecurityTokenServiceProvider")
    fun getSecurityTokenServiceProvider(@RequestParam parameters: MultiValueMap<String, String>, @RequestBody body: String): String {
        log.debug("SecurityTokenServiceProvider request body: $body")
        val config =
                "<ns2:Envelope xmlns:ns2=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "    <response>\n" +
                        "        <issuer>https://digisos.labs.nais.io/</issuer>\n" +
                        "        <subject_types_supported>[]</subject_types_supported>\n" +
                        "        <jwks_uri>https://digisos.labs.nais.io/</jwks_uri>\n" +
                        "    </response>\n" +
                        "</ns2:Envelope>"
        //config.put("subject_types_supported", listOf("public", "pairwise"))
        log.info("Henter SecurityTokenServiceProvider:\n$config")
        return config
    }
}
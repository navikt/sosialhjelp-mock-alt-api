package no.nav.sbl.sosialhjelp_mock_alt.integrations.appgw

import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.FssToken
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraHeaders
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AppGatewaySTSController(
    private val mockOAuth2Server: MockOAuth2Server
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/appgw/")
    fun getAaregSts(@RequestHeader headers: HttpHeaders): ResponseEntity<FssToken> {
        val fnr = hentFnrFraHeaders(headers)
        val fssToken = FssToken(mockOAuth2Server.issueToken("selvbetjening", fnr, "someaudience").serialize(), "FssToken", 120L)
        log.info("Henter appgw token: ${objectMapper.writeValueAsString(fssToken)}")
        return ResponseEntity.ok(fssToken)
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.integrations.appgw

import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.ArbeidsforholdDto
import no.nav.sbl.sosialhjelp_mock_alt.integrations.sts.StsController
import no.nav.sbl.sosialhjelp_mock_alt.integrations.aareg.model.FssToken
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.hentFnrFraToken
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.token.support.test.JwtTokenGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AppGatewaySTSController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/appgw/")
    fun getAaregSts(@RequestHeader headers: HttpHeaders): ResponseEntity<FssToken> {
        val fnr = hentFnrFraToken(headers)
        val fssToken = FssToken.createToken(fnr)
        log.info("Henter appgw token: ${objectMapper.writeValueAsString(fssToken)}")
        return ResponseEntity.ok(fssToken)
    }
}

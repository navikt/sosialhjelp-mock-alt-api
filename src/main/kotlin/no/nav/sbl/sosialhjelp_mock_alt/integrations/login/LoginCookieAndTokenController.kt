package no.nav.sbl.sosialhjelp_mock_alt.integrations.login

import no.nav.sbl.sosialhjelp_mock_alt.utils.fastFnr
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.test.FileResourceRetriever
import no.nav.security.token.support.test.JwtTokenGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class LoginCookieAndTokenController(
    @Value("\${host_address}") private val host_address: String,
    @Value("\${cookie_domain}") private val cookie_domain: String,
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/login/metadata")
    fun getMockAltMetadate(): String {
        val fileResourceRetriever = FileResourceRetriever("/metadata.json", "/jwkset.json")
        val retrieveResource = fileResourceRetriever.retrieveResource(URL("http://metadata"))
        val metadata = retrieveResource.content.replace("http://jwks", "${host_address}sosialhjelp/mock-alt-api/local/jwks")
        log.info("Henter metadata:\n$metadata")
        return metadata
    }

    @Unprotected
    @GetMapping("/login/cookie")
    fun addCookie(
        @RequestParam(value = "subject") subject: String?,
        @RequestParam(value = "cookiename", defaultValue = "localhost-idtoken") cookieName: String?,
        @RequestParam(value = "redirect", required = false) redirect: String?,
        @RequestParam(value = "expiry", required = false) expiry: String?,
        request: HttpServletRequest?,
        response: HttpServletResponse
    ): Cookie? {
        val defaultHours = 12
        val expiryTime = expiry?.toLong() ?: (60 * defaultHours).toLong()
        val token = JwtTokenGenerator.createSignedJWT(subject ?: fastFnr, expiryTime)
        val cookie = Cookie(cookieName, token.serialize())
        cookie.domain = cookie_domain
        cookie.path = "/"
        response.addCookie(cookie)
        if (redirect != null) {
            response.sendRedirect(redirect)
            return null
        }
        return cookie
    }
}

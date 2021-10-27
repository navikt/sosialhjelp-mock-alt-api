package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.loginApi

import no.nav.sbl.sosialhjelp_mock_alt.config.CORSFilter
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.util.Date
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class LogginApiController(
    private val restTemplate: RestTemplate,
    private val pdlService: PdlService,
    @Value("\${loginurl}") private val loginurl: String,
) {
    companion object {
        private val log by logger()

        fun extractToken(cookie: List<String>): String {
            return cookie.first().split("localhost-idtoken=")[1].split(";")[0]
        }
    }

    @RequestMapping("/login-api/**")
    @ResponseBody
    @Throws(URISyntaxException::class)
    fun soknadProxy(@RequestBody(required = false) body: String?, method: HttpMethod, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ByteArray> {
        log.debug("SoknadProxy request for path: ${request.servletPath}, metode: $method, metode fra request: ${request.method}, body: $body")
        log.debug("SoknadProxy request: $request")
        try {
            checkAuthorized(getHeaders(request))
        } catch (e: MockAltException) {
            return redirectToLoginPage()
        }
        if (request is MultipartHttpServletRequest) {
            return sendRequests(getMultipartBody(request), method, request, response)
        }
        val eksternResponse = sendRequests(body, method, request, response)
        log.debug("SoknadProxy response: $eksternResponse")
        log.debug(
            "SoknadProxy response statuscode: ${eksternResponse.statusCodeValue}, " +
                "body: ${eksternResponse.body?.size},  " +
                "headers: ${objectMapper.writeValueAsString(eksternResponse.headers)}"
        )
        return eksternResponse
    }

    data class UnauthorizedMelding(val id: String, val message: String, val loginUrl: String)

    private fun checkAuthorized(headers: HttpHeaders) {
        val cookie = headers[HttpHeaders.COOKIE]
        if (cookie == null || cookie.isEmpty()) {
            log.info("Unauthorized: No Cookie!")
            throw MockAltException("Unauthorized: No Cookie!")
        } else {
            try {
                val tokenString = extractToken(cookie)
                if (tokenString.isEmpty())
                    log.debug("Could not extract token from cookie: ${objectMapper.writeValueAsString(cookie)}")
                val jwtToken = JwtToken(tokenString)
                val expirationDate = jwtToken.jwtTokenClaims.expirationTime
                if (Date().after(expirationDate)) {
                    log.info("Unauthorized: Token has expired: $expirationDate")
                    throw MockAltException("Unauthorized: Token has expired: $expirationDate")
                }
                val fnr = jwtToken.subject
                if (!pdlService.finnesPersonMedFnr(fnr)) {
                    log.info("Unauthorized: Unknown subject: $fnr")
                    throw MockAltException("Unauthorized: Unknown subject: $fnr")
                }
                log.debug("Authorized ok med fnr: $fnr")
            } catch (e: IndexOutOfBoundsException) {
                log.info("Unauthorized: Bad Cookie: ${e.message}")
                throw MockAltException("Unauthorized: Bad Cookie: ${e.message}")
            }
        }
    }

    private fun sendRequests(body: Any?, method: HttpMethod, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ByteArray> {
        var newUri = request.requestURL.append(getQueryString(request)).toString()

        newUri = newUri.replace("/sosialhjelp/mock-alt-api/login-api", "")
        newUri = if (newUri.contains("innsyn-api")) {
            newUri.replace("localhost:8989", "localhost:8080")
        } else {
            newUri.replace("localhost:8989", "localhost:8181")
        }
        newUri = newUri.replace("sosialhjelp-mock-alt-api-gcp.dev.nav.no", "digisos-gcp.dev.nav.no")
        newUri = newUri.replace("sosialhjelp-mock-alt-api.labs.nais.io", "digisos.labs.nais.io")

        val headers = getHeaders(request)
        addAccessTokenHeader(headers)
        fixCorsHeadersInResponse(request, response)

        log.debug("sendRequests newUri: $newUri")
        try {
            return restTemplate.exchange(newUri, method, HttpEntity(body, headers), ByteArray::class.java)
        } catch (e: HttpClientErrorException) {
            if (e.message?.contains("Unauthorized: 401 ") == true) {
                throw MockAltException("Unauthorized: Client reported 401.")
            }
            throw e
        }
    }

    private fun getQueryString(request: HttpServletRequest): String {
        val queryString = if (request.queryString != null) {
            "?${request.queryString}"
        } else {
            ""
        }
        return queryString
    }

    private fun redirectToLoginPage(): ResponseEntity<ByteArray> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(objectMapper.writeValueAsString(UnauthorizedMelding("azuread_authentication_error", "Autentiseringsfeil", loginurl)).toByteArray())
    }

    private fun getHeaders(request: HttpServletRequest): HttpHeaders {
        val httpHeaders = HttpHeaders()
        val headerNames = request.headerNames

        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            val headers = request.getHeaders(headerName)
            while (headers.hasMoreElements()) {
                httpHeaders.add(headerName, headers.nextElement())
            }
        }
        return httpHeaders
    }

    private fun fixCorsHeadersInResponse(request: HttpServletRequest, response: HttpServletResponse) {
        response.reset()
        CORSFilter.setAllowOriginHeader(request, response)
    }

    private fun addAccessTokenHeader(httpHeaders: HttpHeaders): HttpHeaders {
        val cookie = httpHeaders[HttpHeaders.COOKIE]
        if (cookie != null && cookie.isNotEmpty()) {
            val token = extractToken(cookie)
            httpHeaders.setBearerAuth(token)
            httpHeaders.remove(HttpHeaders.COOKIE)
        }
        return httpHeaders
    }

    private fun getMultipartBody(request: MultipartHttpServletRequest): LinkedMultiValueMap<String, Any> {
        val multipartBody = LinkedMultiValueMap<String, Any>()
        request.fileNames.forEach { name ->
            val files: MutableList<MultipartFile> = request.getFiles(name)
            files.forEach {
                multipartBody.add(name, MultipartInputStreamFileResource(it.inputStream, it.originalFilename))
            }
        }
        return multipartBody
    }

    internal inner class MultipartInputStreamFileResource(inputStream: InputStream, private val filename: String?) : InputStreamResource(inputStream) {

        override fun getFilename(): String? {
            return this.filename
        }

        @Throws(IOException::class)
        override fun contentLength(): Long {
            return -1 // we do not want to generally read the whole stream into memory ...
        }
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.loginApi

import no.nav.sbl.sosialhjelp_mock_alt.config.CORSFilter
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Autowired
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
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class LogginApiController(
        @Autowired val restTemplate: RestTemplate,
        @Autowired val pdlService: PdlService,
        @Value("\${loginurl}") private val loginurl: String,
) {
    companion object {
        private val log by logger()
    }

    @RequestMapping("/login-api/**")
    @ResponseBody
    @Throws(URISyntaxException::class)
    fun soknadProxy(@RequestBody(required = false) body: String?, method: HttpMethod, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ByteArray> {
        log.debug("SoknadProxy request for path: ${request.servletPath}, metode: $method, metode fra request: ${request.method}, body: $body")
        log.debug("SoknadProxy request: $request")
        try {
            checkAuthorized(getHeaders(request))
        } catch (e: RuntimeException) {
            log.warn(e.message)
            if (e.message?.startsWith("Unauthorized: ") != true) log.debug(e.stackTraceToString())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(UnauthorizedMelding("azuread_authentication_error", "Autentiseringsfeil", loginurl)).toByteArray())
        }
        if (request is MultipartHttpServletRequest) {
            return sendRequests(getMultipartBody(request), method, request, response)
        }
        val eksternResponse = sendRequests(body, method, request, response)
        log.debug("SoknadProxy response: $eksternResponse")
        log.debug("SoknadProxy response statuscode: ${eksternResponse.statusCodeValue}, body: ${eksternResponse.body},  headers: ${eksternResponse.headers}")
        return eksternResponse
    }

    data class UnauthorizedMelding(val id: String, val message: String, val loginUrl: String)

    private fun checkAuthorized(headers: HttpHeaders) {
        val cookie = headers[HttpHeaders.COOKIE]
        if (cookie == null || cookie.isEmpty()) {
            throw RuntimeException("Unauthorized: No Cookie!")
        } else {
            val tokenString = cookie.first().replace("localhost-idtoken=", "").split(";")[0]
            val fnr = JwtToken(tokenString).subject
            if (!pdlService.personListe.containsKey(key = fnr)) {
                throw RuntimeException("Unauthorized: Unknown subject in token: $fnr")
            }
        }
    }

    fun sendRequests(body: Any?, method: HttpMethod, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ByteArray> {
        var newUri = request.requestURL.toString().replace("/sosialhjelp/mock-alt-api/login-api", "")
        newUri = newUri.replace("localhost:8989", "localhost:8181")

        val headers = getHeaders(request)
        addAccessTokenHeader(headers)
        fixCorsHeadersInResponse(request, response)

        return restTemplate.exchange(newUri, method, HttpEntity(body, headers), ByteArray::class.java)
    }

    fun getHeaders(request: HttpServletRequest): HttpHeaders {
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

    fun fixCorsHeadersInResponse(request: HttpServletRequest, response: HttpServletResponse) {
        response.reset()
        CORSFilter.setAllowOriginHeader(request, response)
    }


    fun addAccessTokenHeader(httpHeaders: HttpHeaders): HttpHeaders {
        val cookie = httpHeaders[HttpHeaders.COOKIE]
        if (cookie != null && cookie.isNotEmpty()) {
            val token = cookie.first().replace("localhost-idtoken=", "").split(";")[0]
            httpHeaders.setBearerAuth(token)
            httpHeaders[HttpHeaders.COOKIE] = null
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

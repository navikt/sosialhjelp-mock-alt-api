package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.loginApi

import jakarta.servlet.annotation.MultipartConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URISyntaxException
import java.util.Date
import no.nav.sbl.sosialhjelp_mock_alt.config.CORSFilter
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.MockAltException
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
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

@RestController
@MultipartConfig(
    fileSizeThreshold = 5 * 1024 * 1024,
    maxFileSize = 20 * 1024 * 1024,
    maxRequestSize = 150 * 1024 * 1024,
    location = "/tmp")
class LogginApiController(
    private val restTemplate: RestTemplate,
    private val pdlService: PdlService,
    @param:Value("\${loginurl}") private val loginurl: String,
    @param:Value("\${soknad-api-via-docker-compose}") private val soknadApiViaDockerCompose: Boolean,
    @param:Value("\${innsyn-api-via-docker-compose}") private val innsynApiViaDockerCompose: Boolean,
    @param:Value("\${modia-api-via-docker-compose}") private val modiaApiViaDockerCompose: Boolean,
) {
  companion object {
    private val log by logger()

    private const val soknadApiDockerComposeHost = "sosialhjelp-soknad-api.digisos.docker-internal"
    private const val innsynApiDockerComposeHost = "sosialhjelp-innsyn-api.digisos.docker-internal"
    private const val modiaApiDockerComposeHost = "sosialhjelp-modia-api.digisos.docker-internal"
  }

  @RequestMapping("/login-api/**")
  @ResponseBody
  @Throws(URISyntaxException::class)
  fun soknadProxy(
      @RequestBody(required = false) body: String?,
      method: HttpMethod,
      request: HttpServletRequest,
      response: HttpServletResponse,
  ): ResponseEntity<ByteArray> {
    log.debug(
        "SoknadProxy request for path: ${request.servletPath}, metode: $method, metode fra request: ${request.method}, body: $body")
    log.debug("SoknadProxy request: $request")
    try {
      checkAuthorized(request)
    } catch (e: MockAltException) {
      return redirectToLoginPage()
    }
    if (request is MultipartHttpServletRequest) {
      return sendRequests(getMultipartBody(request), method, request, response)
    }
    val eksternResponse = sendRequests(body, method, request, response)
    if (eksternResponse.headers.containsKey("Access-Control-Allow-Credentials")) {
      response.reset()
      val origin = request.getHeader("Origin") ?: "*"
      response.setHeader("Access-Control-Allow-Origin", origin)
    }
    log.debug("SoknadProxy response: $eksternResponse")
    log.debug(
        "SoknadProxy response statuscode: ${eksternResponse.statusCode}, " +
            "body: ${eksternResponse.body?.size},  " +
            "headers: ${objectMapper.writeValueAsString(eksternResponse.headers)}")
    return eksternResponse
  }

  data class UnauthorizedMelding(val id: String, val message: String, val loginUrl: String)

  private fun checkAuthorized(request: HttpServletRequest) {
    val cookie = request.cookies
    if (cookie.isNullOrEmpty() || request.getHeader("Authorization") == null) {
      log.info("Unauthorized: No Cookie or auth header!")
      throw MockAltException("Unauthorized: No Cookie or Auth header!")
    } else {
      try {
        val tokenString =
            cookie.firstOrNull { it.name == "localhost-idtoken" }?.value
                ?: request.getHeader("Authorization")?.removePrefix("Bearer ")
        if (tokenString == null) {
          log.debug(
              "Could not extract token from cookie: ${objectMapper.writeValueAsString(cookie)}")
          throw MockAltException("Unauthorized: No Cookie!")
        }
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

  private fun sendRequests(
      body: Any?,
      method: HttpMethod,
      request: HttpServletRequest,
      response: HttpServletResponse,
  ): ResponseEntity<ByteArray> {
    var newUri = request.requestURL.append(getQueryString(request)).toString()
    newUri = newUri.replace("/sosialhjelp/mock-alt-api/login-api", "")
    newUri =
        when {
          newUri.contains("soknad-api") && soknadApiViaDockerCompose ->
              newUri.replace("localhost:8989", "$soknadApiDockerComposeHost:8080")
          // Ikke Docker compose varianten av soknads-api blir fikset i else
          newUri.contains("innsyn-api") && innsynApiViaDockerCompose ->
              newUri.replace("localhost:8989", "$innsynApiDockerComposeHost:8080")
          newUri.contains("innsyn-api") && !innsynApiViaDockerCompose ->
              newUri.replace("localhost:8989", "localhost:8080")
          newUri.contains("modia-api") && modiaApiViaDockerCompose ->
              newUri.replace("localhost:8989", "$modiaApiDockerComposeHost:8080")
          newUri.contains("modia-api") && !modiaApiViaDockerCompose ->
              newUri.replace("localhost:8989", "localhost:8383")
          else -> newUri.replace("localhost:8989", "localhost:8181")
        }

    if (newUri.contains("innsyn-api")) {
      newUri =
          newUri.replace(
              "https://sosialhjelp-mock-alt-api-mock.ekstern.dev.nav.no",
              "http://sosialhjelp-innsyn-api-mock")
    } else if (newUri.contains("soknad-api")) {
      newUri =
          newUri.replace(
              "https://sosialhjelp-mock-alt-api-mock.ekstern.dev.nav.no",
              "http://sosialhjelp-soknad-api-mock")
    }

    val headers = getHeaders(request)

    headers.remove("content-length")
    addAccessTokenHeader(request, headers)
    fixCorsHeadersInResponse(request, response)

    log.debug("sendRequests newUri: $newUri")
    return try {
      restTemplate.exchange(newUri, method, HttpEntity(body, headers), ByteArray::class.java)
    } catch (e: HttpClientErrorException) {
      ResponseEntity.status(e.statusCode).body(e.responseBodyAsByteArray)
    }
  }

  private fun getQueryString(request: HttpServletRequest): String {
    val queryString =
        if (request.queryString != null) {
          "?${request.queryString}"
        } else {
          ""
        }
    return queryString
  }

  private fun redirectToLoginPage(): ResponseEntity<ByteArray> {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper
                .writeValueAsString(
                    UnauthorizedMelding(
                        "azuread_authentication_error", "Autentiseringsfeil", loginurl))
                .toByteArray())
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
    CORSFilter.setCorsHeaders(request, response)
  }

  private fun addAccessTokenHeader(request: HttpServletRequest, httpHeaders: HttpHeaders) {
    val cookie = request.cookies
    if (cookie != null && cookie.isNotEmpty()) {
      val token = cookie.first { it.name == "localhost-idtoken" }.value
      val xsrfCookie = cookie.firstOrNull { it.name == "XSRF-TOKEN-INNSYN-API" }?.value
      httpHeaders.setBearerAuth(token)

      if (xsrfCookie.isNullOrEmpty()) {
        httpHeaders.remove(HttpHeaders.COOKIE)
      } else {
        httpHeaders.set(HttpHeaders.COOKIE, "XSRF-TOKEN-INNSYN-API=$xsrfCookie")
      }
    }
  }

  private fun getMultipartBody(
      request: MultipartHttpServletRequest,
  ): LinkedMultiValueMap<String, Any> {
    val multipartBody = LinkedMultiValueMap<String, Any>()
    request.fileNames.forEach { name ->
      val files: MutableList<MultipartFile> = request.getFiles(name)
      files.forEach { file -> multipartBody.add(name, file.resource) }
    }
    return multipartBody
  }

  internal inner class MultipartInputStreamFileResource(
      byteArray: ByteArray,
      private val filename: String?,
  ) : ByteArrayResource(byteArray) {

    override fun getFilename(): String? {
      return this.filename
    }
  }
}

package no.nav.sbl.sosialhjelp.mock.alt.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException

class CORSFilter : Filter {
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig?) {}

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain,
    ) {
        val httpResponse = servletResponse as HttpServletResponse

        setCorsHeaders(servletRequest, servletResponse)

        filterChain.doFilter(servletRequest, httpResponse)
    }

    override fun destroy() {}

    companion object {
        fun setCorsHeaders(
            servletRequest: ServletRequest,
            httpResponse: HttpServletResponse,
        ): String {
            val origin =
                if (servletRequest is HttpServletRequest) {
                    (servletRequest.getHeader("Origin") ?: "*")
                } else {
                    "*"
                }
            httpResponse.setHeader("Access-Control-Allow-Origin", origin)
            httpResponse.setHeader(
                "Access-Control-Allow-Headers",
                "Origin, Content-Type, Accept, X-XSRF-TOKEN, XSRF-TOKEN-INNSYN-API, Authorization, Nav-Call-Id",
            )
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
            return origin
        }
    }
}

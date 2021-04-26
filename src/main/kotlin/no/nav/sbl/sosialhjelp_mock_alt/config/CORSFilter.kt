package no.nav.sbl.sosialhjelp_mock_alt.config

import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CORSFilter : Filter {

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig?) {
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpResponse = servletResponse as HttpServletResponse
        val origin = if (servletRequest is HttpServletRequest) (servletRequest.getHeader("Origin")) else null

        httpResponse.setHeader("Access-Control-Allow-Origin", origin ?: "*")
        httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-XSRF-TOKEN, Authorization, Nav-Call-Id")
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
        filterChain.doFilter(servletRequest, httpResponse)
    }

    override fun destroy() {}

    companion object {
        fun setAllowOriginHeader(servletRequest: ServletRequest, httpResponse: HttpServletResponse): String {
            val origin = if (servletRequest is HttpServletRequest) (servletRequest.getHeader("Origin") ?: "*") else "*"
            httpResponse.setHeader("Access-Control-Allow-Origin", origin)
            return origin
        }
    }
}

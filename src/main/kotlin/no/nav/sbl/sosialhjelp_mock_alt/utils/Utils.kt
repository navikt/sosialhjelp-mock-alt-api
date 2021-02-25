package no.nav.sbl.sosialhjelp_mock_alt.utils

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp_mock_alt.datastore.pdl.model.Kjoenn
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.reflect.full.companionObject

val fastFnr = genererTilfeldigPersonnummer(dato = LocalDate.of(1945, 10, 26), Kjoenn.KVINNE)

fun String.toLocalDateTime(): LocalDateTime {
    return ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}

fun unixToLocalDateTime(tidspunkt: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(tidspunkt), ZoneId.of("Europe/Oslo"))
}

fun LocalDate.toIsoString(): String {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(this)
}

fun hentFnrFraBody(body: String?): String? {
    if (body != null) {
        val bodyMap: Map<String, String> = objectMapper.readValue(body)
        return bodyMap.get("fnr")
    }
    //  TODO throw exception?
    return fastFnr
}

fun hentFnrFraHeaders(headers: HttpHeaders): String {
    val fnrString = headers["nav-personident"]
    if (fnrString != null) {
        val fnr = fnrString.first()
        if (fnr != null) {
            return fnr
        }
    }
    val fnrListe = headers["nav-personidenter"]
    if (fnrListe != null) {
        return fnrListe.firstOrNull() ?: fastFnr
    }
    return fastFnr
}

fun hentFnrFraToken(headers: HttpHeaders): String {
    val token = headers[HttpHeaders.AUTHORIZATION]
    if (token != null) {
        if (token.isNotEmpty()) {
            val tokenString = token.first().split(" ")[1]
            return JwtToken(tokenString).subject
        }
    }
    return fastFnr
}

fun randomInt(length: Int): Int {
    return (Math.random() * 10.0.pow(length.toDouble())).roundToInt()
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

// unwrap companion class to enclosing class given a Java Class
private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

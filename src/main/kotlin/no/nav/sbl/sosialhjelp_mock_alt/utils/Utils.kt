package no.nav.sbl.sosialhjelp_mock_alt.utils

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.companionObject

val fastFnr = genererTilfeldigPersonnummer()

fun String.toLocalDateTime(): LocalDateTime {
    return ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}
fun unixToLocalDateTime(tidspunkt: Long): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(tidspunkt), ZoneId.of("Europe/Oslo"))
}

fun hentFnrFraBody(body: String?): String? {
    if(body != null) {
        val bodyMap: Map<String, String> = objectMapper.readValue(body)
        return bodyMap.get("fnr")
    }
    //  TODO throw exception?
    return fastFnr
}

fun hentFnrFraToken(): String {
    // TODO: Les fnr fra token.
    return fastFnr
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

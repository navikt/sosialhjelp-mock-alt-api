package no.nav.sbl.sosialhjelp.mock.alt.datastore.skjermedepersoner

import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.stereotype.Component

@Component
class SkjermedePersonerService {
    private val skjermedePersonerMap: HashMap<String, Boolean> = HashMap()

    fun getStatus(ident: String): Boolean {
        log.info("Henter skermingsstatus for $ident")
        return skjermedePersonerMap[ident] ?: false
    }

    fun setStatus(
        ident: String,
        status: Boolean,
    ) {
        skjermedePersonerMap[ident] = status
    }

    companion object {
        private val log by logger()
    }
}

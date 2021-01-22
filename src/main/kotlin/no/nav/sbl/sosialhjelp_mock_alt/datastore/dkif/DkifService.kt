package no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif

import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfoBolk
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class DkifService {

    final val organisasjonNoekkelinfoMap: HashMap<String, DigitalKontaktinfo> = HashMap()

    fun putDigitalKontaktinfo(fnr: String, digitalKontaktinfo: DigitalKontaktinfo) {
        organisasjonNoekkelinfoMap[fnr] = digitalKontaktinfo
    }

    fun getDigitalKontaktinfo(fnr: String): DigitalKontaktinfo? {
        return organisasjonNoekkelinfoMap[fnr]
    }
    fun getDigitalKontaktinfoBolk(fnr: String): DigitalKontaktinfoBolk? {
        return DigitalKontaktinfoBolk(mapOf(fnr to organisasjonNoekkelinfoMap[fnr]!!), null)
    }

    companion object {
        private val log by logger()
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif

import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfo
import no.nav.sbl.sosialhjelp_mock_alt.datastore.dkif.model.DigitalKontaktinfoBolk
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt
import org.springframework.stereotype.Service

@Service
class DkifService {

    final val kontaktinfoMap: HashMap<String, DigitalKontaktinfo> = HashMap()

    fun putDigitalKontaktinfo(fnr: String, digitalKontaktinfo: DigitalKontaktinfo) {
        kontaktinfoMap[fnr] = digitalKontaktinfo
    }

    fun getDigitalKontaktinfo(fnr: String): DigitalKontaktinfo? {
        return kontaktinfoMap[fnr]
    }
    fun getDigitalKontaktinfoBolk(fnr: String): DigitalKontaktinfoBolk? {
        val kontaktinfo = kontaktinfoMap[fnr] ?: DigitalKontaktinfo(randomInt(8).toString())
        kontaktinfoMap[fnr] = kontaktinfo
        return DigitalKontaktinfoBolk(mapOf(fnr to kontaktinfo), null)
    }

    companion object {
        private val log by logger()
    }
}

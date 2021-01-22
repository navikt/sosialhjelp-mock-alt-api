package no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte

import no.nav.sbl.sosialhjelp_mock_alt.datastore.bostotte.model.BostotteDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class BostotteService {

    final val bostotteMap: HashMap<String, BostotteDto> = HashMap()

    fun putBostotte(fnr: String, bostotteDto: BostotteDto) {
        bostotteMap[fnr] = bostotteDto
    }

    fun getBostotte(fnr: String): BostotteDto {
        val bostotteDto = bostotteMap[fnr]
        if(bostotteDto != null)
            return bostotteDto
        return BostotteDto()
    }

    companion object {
        private val log by logger()
    }
}

package no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.OrganisasjonNoekkelinfoDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class EregService {

    final val organisasjonNoekkelinfoMap: HashMap<String, OrganisasjonNoekkelinfoDto> = HashMap()

    fun putOrganisasjonNoekkelinfo(fnr: String, arbeidsforholdDto: OrganisasjonNoekkelinfoDto) {
        organisasjonNoekkelinfoMap[fnr] = arbeidsforholdDto
    }

    fun getOrganisasjonNoekkelinfo(fnr: String): OrganisasjonNoekkelinfoDto? {
        return organisasjonNoekkelinfoMap[fnr]
    }

    companion object {
        private val log by logger()
    }
}

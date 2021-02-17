package no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.OrganisasjonNoekkelinfoDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class EregService {

    final val organisasjonNoekkelinfoMap: HashMap<String, OrganisasjonNoekkelinfoDto> = HashMap()

    fun putOrganisasjonNoekkelinfo(orgNr: String, arbeidsforholdDto: OrganisasjonNoekkelinfoDto) {
        organisasjonNoekkelinfoMap[orgNr] = arbeidsforholdDto
    }

    fun getOrganisasjonNoekkelinfo(orgNr: String): OrganisasjonNoekkelinfoDto? {
        return organisasjonNoekkelinfoMap[orgNr]
    }

    companion object {
        private val log by logger()
    }
}

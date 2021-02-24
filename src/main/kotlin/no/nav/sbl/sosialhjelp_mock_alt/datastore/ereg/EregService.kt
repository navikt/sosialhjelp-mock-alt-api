package no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.NavnDto
import no.nav.sbl.sosialhjelp_mock_alt.datastore.ereg.model.OrganisasjonNoekkelinfoDto
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class EregService {

    private val organisasjonNoekkelinfoMap: HashMap<String, OrganisasjonNoekkelinfoDto> = HashMap()

    fun getOrganisasjonNoekkelinfo(orgNr: String): OrganisasjonNoekkelinfoDto? {
        log.info("Henter OrganisasjonNoekkelinfo for orgNr: $orgNr")
        return organisasjonNoekkelinfoMap[orgNr]
    }

    fun putOrganisasjonNoekkelinfo(orgnummer: String, orgnavn: String) {
        organisasjonNoekkelinfoMap[orgnummer] = OrganisasjonNoekkelinfoDto(
                navn = NavnDto(orgnavn),
                organisasjonsnummer = orgnummer,
        )
    }

    companion object {
        private val log by logger()
    }
}

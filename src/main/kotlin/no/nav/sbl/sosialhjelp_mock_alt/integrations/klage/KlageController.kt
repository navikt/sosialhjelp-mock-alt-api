package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.KlageService
import org.springframework.web.bind.annotation.RestController

@RestController
class KlageController(
    private val klageService: KlageService,
) {

}



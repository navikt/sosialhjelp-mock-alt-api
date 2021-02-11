package no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten

import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service

@Service
class SkatteetatenService {

    final val skattelisten: HashMap<String, SkattbarInntekt> = HashMap()

    fun putSkattbarInntekt(fnr: String, skattbarInntekt: SkattbarInntekt) {
        skattelisten[fnr] = skattbarInntekt
    }

    fun getSkattbarInntekt(fnr: String): SkattbarInntekt {
        return skattelisten[fnr] ?: SkattbarInntekt()
    }

    companion object {
        private val log by logger()
    }
}

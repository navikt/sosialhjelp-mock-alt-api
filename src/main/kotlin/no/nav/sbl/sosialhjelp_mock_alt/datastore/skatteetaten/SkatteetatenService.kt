package no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten

import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.OppgaveInntektsmottaker
import no.nav.sbl.sosialhjelp_mock_alt.datastore.skatteetaten.model.SkattbarInntekt
import org.springframework.stereotype.Service

@Service
class SkatteetatenService {

  private val skattelisten: HashMap<String, SkattbarInntekt> = HashMap()
  private val autoGenerationSet: HashSet<String> = HashSet()

  fun putSkattbarInntekt(fnr: String, skattbarInntekt: SkattbarInntekt) {
    skattelisten[fnr] = skattbarInntekt
  }

  fun getSkattbarInntekt(fnr: String): SkattbarInntekt {
    if (autoGenerationSet.contains(fnr)) {
      return SkattbarInntekt.Builder()
          .leggTilOppgave(OppgaveInntektsmottaker.Builder().standardOppgave().build())
          .build()
    }
    return skattelisten[fnr] ?: SkattbarInntekt()
  }

  fun enableAutoGenerationFor(fnr: String) {
    autoGenerationSet.add(fnr)
  }
}

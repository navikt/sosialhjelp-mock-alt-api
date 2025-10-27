package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.Kontaktpersoner
import org.springframework.stereotype.Service

@Service
class KommuneInfoService {
  private val kommuneInfoMap: HashMap<String, KommuneInfo> = HashMap()

  fun hentAlleKommuner(): Collection<KommuneInfo> {
    return kommuneInfoMap.values
  }

  fun getKommuneInfo(kommunenummer: String): KommuneInfo {
    return kommuneInfoMap[kommunenummer] ?: lagKommuneInfo(kommunenummer)
  }

  fun addKommunieInfo(kommunenummer: String) {
    kommuneInfoMap[kommunenummer] = lagKommuneInfo(kommunenummer)
  }

  fun addSvarutKommuneInfo(kommunenummer: String) {
    kommuneInfoMap[kommunenummer] = lagSvarUtKommune(kommunenummer)
  }

  private fun lagSvarUtKommune(id: String) =
      KommuneInfo(
          kommunenummer = id,
          kanMottaSoknader = false,
          kanOppdatereStatus = false,
          harMidlertidigDeaktivertOppdateringer = false,
          harMidlertidigDeaktivertMottak = false,
          kontaktpersoner =
              Kontaktpersoner(listOf("Kontakt$id@navo.no"), listOf("Test$id@navno.no")),
          harNksTilgang = false,
          behandlingsansvarlig = null,
      )

  private fun lagKommuneInfo(id: String) =
      KommuneInfo(
          kommunenummer = id,
          kanMottaSoknader = true,
          kanOppdatereStatus = true,
          harMidlertidigDeaktivertOppdateringer = false,
          harMidlertidigDeaktivertMottak = false,
          kontaktpersoner =
              Kontaktpersoner(listOf("Kontakt$id@navo.no"), listOf("Test$id@navno.no")),
          harNksTilgang = true,
          behandlingsansvarlig = null,
      )
}

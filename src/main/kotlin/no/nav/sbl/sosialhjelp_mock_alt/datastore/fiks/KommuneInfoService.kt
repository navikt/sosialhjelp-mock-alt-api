package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.Kontaktpersoner
import org.springframework.stereotype.Service
import java.util.Collections

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

    private fun lagKommuneInfo(id: String) = KommuneInfo(
        kommunenummer = id,
        kanMottaSoknader = true,
        kanOppdatereStatus = true,
        harMidlertidigDeaktivertOppdateringer = false,
        harMidlertidigDeaktivertMottak = false,
        kontaktpersoner = Kontaktpersoner(
            Collections.singletonList("Kontakt$id@navo.no"),
            Collections.singletonList("Test$id@navno.no")
        ),
        harNksTilgang = true,
        behandlingsansvarlig = null
    )
}

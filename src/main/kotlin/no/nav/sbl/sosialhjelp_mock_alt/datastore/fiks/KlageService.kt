package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import org.springframework.stereotype.Service

@Service
class KlageService {
  private val klager: MutableMap<FiksDigisosId, MutableList<Klage>> = mutableMapOf()

  fun leggTilKlage(fiksDigisosId: FiksDigisosId, klage: Klage) =
    klager.getOrPut(fiksDigisosId) { mutableListOf() }.add(klage)


  fun hentKlager(fiksDigisosId: FiksDigisosId): List<Klage> = klager[fiksDigisosId] ?: emptyList()
}

@JvmInline
value class FiksDigisosId(val value: String)

data class Klage(val fiksDigisosId: String, val klageTekst: String, val vedtaksIds: List<String>)

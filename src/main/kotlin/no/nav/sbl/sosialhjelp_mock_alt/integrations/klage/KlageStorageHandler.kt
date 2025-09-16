package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID

class KlageStorageHandler {

  private val storage: MutableMap<String, MutableList<FiksKlageDto>> = mutableMapOf()

  fun get(personId: String): List<FiksKlageDto>? = storage[personId]

  fun createKlage(personId: String, klage: FiksKlageDto) {

    val klageList = storage[personId] ?: mutableListOf()
    klageList.validate(klage.klageId, personId)

    klageList.add(klage)

    storage[personId] = klageList
  }
}

private fun MutableList<FiksKlageDto>.validate(klageId: UUID, personId: String) {
  find { it.klageId == klageId }
      ?.let {
        throw IllegalStateException("Klage med id ${klageId} finnes allerede for person $personId")
      }
}

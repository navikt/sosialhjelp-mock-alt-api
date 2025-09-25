package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger

class KlageStorageHandler {

  private val storage: MutableMap<String, MutableList<FiksKlageDto>> = mutableMapOf()

  fun get(personId: String): List<FiksKlageDto>? {
    val klager = storage[personId]

    logger.info("Fant ${klager?.size ?: 0} klager for PersonId: $personId")

    return klager
  }

  fun createKlage(personId: String, klage: FiksKlageDto) {

    val klageList = storage[personId] ?: mutableListOf()
    klageList.validate(klage.klageId, personId)

    klageList.add(klage)

    storage[personId] = klageList

    logger.info("Lagret Klage for PersonId: $personId: $klage")
  }

  companion object {
    private val logger by logger()
  }
}

private fun MutableList<FiksKlageDto>.validate(klageId: UUID, personId: String) {
  find { it.klageId == klageId }
      ?.let {
        throw IllegalStateException("Klage med id ${klageId} finnes allerede for person $personId")
      }
}

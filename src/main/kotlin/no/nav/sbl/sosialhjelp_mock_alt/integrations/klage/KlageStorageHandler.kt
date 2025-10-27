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
    klageList.validateNotExists(klage.klageId, personId)

    klageList.add(klage)

    storage[personId] = klageList

    logger.info("Lagret Klage for PersonId: $personId: $klage")
  }

  fun addEttersendelse(
      personId: String,
      klageId: UUID,
      ettersendelseId: UUID,
      vedleggJsonId: UUID,
      dokumentInfoList: List<DokumentInfoDto>,
  ) {

    logger.info("Legger til ettersendelse for klageId $klageId for person $personId")

    val klager = storage[personId] ?: error("Fant ingen klager for person $personId")
    val klage =
        klager.find { it.klageId == klageId }?.also { klager.remove(it) }
            ?: error("Fant ingen klage med id $klageId for person $personId")

    klage.ettersendtInfoNAV
        .addEttersendelse(ettersendelseId, vedleggJsonId, dokumentInfoList)
        .let { ettersendtInfoNAVDto -> klage.copy(ettersendtInfoNAV = ettersendtInfoNAVDto) }
        .also { updatedKlage -> klager.add(updatedKlage) }

    storage[personId] = klager
  }

  companion object {
    private val logger by logger()
  }
}

private fun MutableList<FiksKlageDto>.validateNotExists(klageId: UUID, personId: String) {
  find { it.klageId == klageId }
      ?.let {
        throw IllegalStateException("Klage med id ${klageId} finnes allerede for person $personId")
      }
}

private fun EttersendtInfoNAVDto.addEttersendelse(
    navEksternRefId: UUID,
    vedleggJsonId: UUID,
    dokumentInfoList: List<DokumentInfoDto>,
): EttersendtInfoNAVDto {
  return ettersendelser
      .plus(
          EttersendelseDto(
              navEksternRefId = navEksternRefId,
              vedleggMetadata = vedleggJsonId,
              vedlegg = dokumentInfoList,
              timestampSendt = System.currentTimeMillis(),
          )
      )
      .let { updatedList -> this.copy(ettersendelser = updatedList) }
}

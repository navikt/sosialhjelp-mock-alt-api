package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FiksKlageService(
    private val mellomlagringService: MellomlagringService,
    private val dokumentlagerService: DokumentlagerService,
) {

  val klageStorage: KlageStorageHandler = KlageStorageHandler()

  fun hentKlager(personId: String, digisosId: UUID?): List<FiksKlageDto> {
    return klageStorage
        .get(personId)
        ?.filter { klage -> digisosId == null || klage.digisosId == digisosId }
        .also { klager ->
          logger.info(
              "Fant ${klager?.size ?: 0} klager for DigisosId: $digisosId på person $personId")
        } ?: emptyList()
  }

  fun handleMottattKlage(
      personId: String,
      digisosId: UUID,
      klageId: UUID,
      navEksternRefId: UUID,
      vedtakId: UUID,
      klageFiles: KlageFiles,
  ) {
    val klageJsonId =
        UUID.randomUUID().also {
          dokumentlagerService.leggTilDokument(it.toString(), klageFiles.klageJson)
        }

    val vedleggJsonId =
        UUID.randomUUID().also {
          dokumentlagerService.leggTilDokument(it.toString(), klageFiles.vedleggJson)
        }

    val klagePdfId =
        UUID.randomUUID().also {
          dokumentlagerService.lagreFil(
              it.toString(),
              klageFiles.klagePdf.originalFilename ?: error("Mangler filnavn på klage.pdf"),
              klageFiles.klagePdf.bytes)
        }

    FiksKlageDto(
            digisosId = digisosId,
            klageId = klageId,
            navEksternRefId = klageId,
            vedtakId = vedtakId,
            klageMetadata = klageJsonId,
            vedleggMetadata = vedleggJsonId,
            klageDokument =
                DokumentInfoDto(
                    filnavn =
                        klageFiles.klagePdf.originalFilename
                            ?: error("Mangler filnavn på klage.pdf"),
                    dokumentlagerDokumentId = klagePdfId,
                    storrelse = klageFiles.klagePdf.size,
                ),
            vedlegg = flyttFilerFraMellomlager(navEksternRefId, klageFiles.vedleggJson),
            sendtKvittering =
                SendtKvitteringDto(
                    sendtKanal = FiksProtokoll.FIKS_IO,
                    meldingId = UUID.randomUUID(),
                    sendtStatus =
                        SendtStatusDto(
                            status = SendtStatus.SENDT,
                            timestamp = System.currentTimeMillis(),
                        ),
                    statusListe = emptyList()),
        )
        .also { klageStorage.createKlage(personId, it) }

    logger.info("Mottatt klage for personId $personId med digisosId $digisosId og klageId $klageId")
  }

  fun handleSendEttersendelse() {}

  fun handleTrekkKlage() {}

  private fun flyttFilerFraMellomlager(klageId: UUID, vedleggJson: String): List<DokumentInfoDto> {

    logger.info("Flytter vedlegg fra mellomlager til dokumentlager for KlageId $klageId")

    val jsonVedlegg =
        objectMapper.readValue(vedleggJson, JsonVedleggSpesifikasjon::class.java).let { vedleggSpec
          ->
          vedleggSpec.vedlegg.find { it.type == "klage" && it.klageId == klageId.toString() }
        } ?: error("Fant ikke vedlegg spesifikasjon for klageId $klageId i vedleggJson")

    if (jsonVedlegg.filer.isEmpty()) {
      logger.info("Ingen referanser til vedlegg i JsonVedleggSpec for klageId $klageId")
      return emptyList()
    }

    logger.info("Fant ${jsonVedlegg.filer.size} vedlegg i JsonVedleggSpec for Klage $klageId")

    val mellomlagredeForKlage =
        mellomlagringService.getAll(klageId.toString())?.mellomlagringMetadataList
            ?: error("Finner ingen mellomlagrede filer for klageId $klageId")

    jsonVedlegg.validate(mellomlagredeForKlage.map { it.filnavn })

    return mellomlagredeForKlage
        .map { dokumentDto ->
          val bytes = mellomlagringService.get(klageId.toString(), dokumentDto.filId)
          dokumentlagerService.lagreFil(dokumentDto.filId, dokumentDto.filnavn, bytes)
          DokumentInfoDto(
              filnavn = dokumentDto.filnavn,
              dokumentlagerDokumentId = UUID.fromString(dokumentDto.filId),
              storrelse = bytes.size.toLong())
        }
        .also {
          logger.info("Sletter filer i Mellomlager for KlageId $klageId")
          mellomlagringService.deleteAll(klageId.toString())
        }
  }

  companion object {
    private val logger by logger()
  }
}

private fun JsonVedlegg.validate(mellomlagredeFilnavn: List<String>) {
  require(filer.all { mellomlagredeFilnavn.contains(it.filnavn) }) {
    "Finnes filer i mellomlager uten referanse i JsonVedleggSpec"
  }
}

data class KlageFiles(
    val klageJson: String,
    val vedleggJson: String,
    val klagePdf: MultipartFile,
)

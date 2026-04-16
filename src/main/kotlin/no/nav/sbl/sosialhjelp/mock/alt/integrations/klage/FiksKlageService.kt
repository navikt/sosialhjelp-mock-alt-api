package no.nav.sbl.sosialhjelp.mock.alt.integrations.klage

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.MellomlagerTilDokumentlagerService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class FiksKlageService(
    private val mellomlagerTilDokumentlagerService: MellomlagerTilDokumentlagerService,
    private val dokumentlagerService: DokumentlagerService,
) {
    val klageStorage: KlageStorageHandler = KlageStorageHandler()

    fun hentKlager(
        personId: String,
        digisosId: UUID?,
    ): List<FiksKlageDto> =
        klageStorage
            .get(personId)
            ?.filter { klage -> digisosId == null || klage.digisosId == digisosId }
            .also { klager ->
                logger.info(
                    "Fant ${klager?.size ?: 0} klager for DigisosId: $digisosId på person $personId",
                )
            } ?: emptyList()

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
                    klageFiles.klagePdf.bytes,
                )
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
            vedlegg = hentOgFlyttKlageVedlegg(navEksternRefId, klageId, klageFiles.vedleggJson),
            sendtKvittering =
                SendtKvitteringDto(
                    sendtKanal = FiksProtokoll.FIKS_IO,
                    meldingId = UUID.randomUUID(),
                    sendtStatus =
                        SendtStatusDto(
                            status = SendtStatus.SENDT,
                            timestamp = System.currentTimeMillis(),
                        ),
                    statusListe = emptyList(),
                ),
        ).also { klageStorage.createKlage(personId, it) }

        logger.info("Mottatt klage for personId $personId med digisosId $digisosId og klageId $klageId")
    }

    fun handleEttersendelse(
        personId: String,
        klageId: UUID,
        ettersendelseId: UUID,
        vedleggJson: String,
    ) {
        UUID.randomUUID().also { vedleggJsonId ->
            dokumentlagerService.leggTilDokument(vedleggJsonId.toString(), vedleggJson)

            val dokumentInfoDtos = hentOgFlyttKlageVedlegg(ettersendelseId, klageId, vedleggJson)

            klageStorage.addEttersendelse(
                personId,
                klageId,
                ettersendelseId,
                vedleggJsonId,
                dokumentInfoDtos,
            )
        }
    }

    fun handleTrekkKlage() {}

    private fun hentOgFlyttKlageVedlegg(
        navEksternRefId: UUID,
        klageId: UUID,
        vedleggJson: String,
    ): List<DokumentInfoDto> {
        logger.info(
            "Flytter vedlegg fra mellomlager til dokumentlager for Klage $klageId og referanse $navEksternRefId",
        )

        val jsonVedlegg =
            objectMapper.readValue(vedleggJson, JsonVedleggSpesifikasjon::class.java).let { vedleggSpec ->
                vedleggSpec.vedlegg.find { it.klageId == klageId.toString() }
            } ?: error("Fant ikke vedlegg spesifikasjon for klageId $klageId i vedleggJson")

        val forventedeFilnavn = jsonVedlegg.filer.mapNotNull { it.filnavn }

        return mellomlagerTilDokumentlagerService
            .flyttFilerFraMellomlager(navEksternRefId.toString(), forventedeFilnavn)
            .map {
                DokumentInfoDto(
                    filnavn = it.filnavn,
                    dokumentlagerDokumentId = UUID.fromString(it.dokumentlagerDokumentId),
                    storrelse = it.storrelse,
                )
            }
    }

    companion object {
        private val logger by logger()
    }
}

data class KlageFiles(
    val klageJson: String,
    val vedleggJson: String,
    val klagePdf: MultipartFile,
)

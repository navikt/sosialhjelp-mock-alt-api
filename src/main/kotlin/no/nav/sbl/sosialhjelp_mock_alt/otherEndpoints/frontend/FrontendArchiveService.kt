package no.nav.sbl.sosialhjelp_mock_alt.otherEndpoints.frontend

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.Ettersendelse
import org.springframework.stereotype.Service

@Service
class FrontendArchiveService(
    private val soknadService: SoknadService,
    private val dokumentlagerService: DokumentlagerService
) {
  fun makeSoknadZip(soknad: DigisosSak): ByteArray {
    val fiksDigisosId = soknad.fiksDigisosId
    val bytebuffer = ByteArrayOutputStream()
    val zipArchive = ZipOutputStream(bytebuffer)

    val soknadJson = dokumentlagerService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.metadata)
    zipArchive.putNextEntry(ZipEntry("soknad.json"))
    zipArchive.write(soknadJson!!.toByteArray())
    zipArchive.closeEntry()

    val vedleggJson =
        dokumentlagerService.hentDokument(fiksDigisosId, soknad.originalSoknadNAV!!.vedleggMetadata)
    zipArchive.putNextEntry(ZipEntry("vedlegg.json"))
    zipArchive.write(vedleggJson!!.toByteArray())
    zipArchive.closeEntry()

    val sendtViaInnsyn =
        soknad.ettersendtInfoNAV?.ettersendelser?.map { it.vedleggMetadata } ?: emptyList()

    soknadService
        .hentVedlegg(soknad)
        .filterNot { it.id in sendtViaInnsyn }
        .forEach {
          dokumentlagerService.hentFil(it.id)?.let { fil ->
            zipArchive.putNextEntry(ZipEntry(fil.filnavn))
            zipArchive.write(fil.bytes)
            zipArchive.closeEntry()
          }
        }

    zipArchive.finish()
    zipArchive.close()
    bytebuffer.close()
    return bytebuffer.toByteArray()
  }

  fun makeEttersendelseZip(soknad: DigisosSak): ByteArray {
    val fiksDigisosId = soknad.fiksDigisosId
    val bytebuffer = ByteArrayOutputStream()
    val zipArchive = ZipOutputStream(bytebuffer)

    val sammenslattVedleggJson =
        slaSammenTilJsonVedleggSpesifikasjon(
            soknad.ettersendtInfoNAV?.ettersendelser, fiksDigisosId)
    zipArchive.putNextEntry(ZipEntry("vedlegg.json"))
    zipArchive.write(objectMapper.writeValueAsBytes(sammenslattVedleggJson))
    zipArchive.closeEntry()

    soknadService.hentEttersendelsePdf(fiksDigisosId)?.let {
      zipArchive.putNextEntry(ZipEntry(it.filnavn))
      zipArchive.write(it.bytes)
      zipArchive.closeEntry()
    }

    soknad.ettersendtInfoNAV
        ?.ettersendelser
        ?.flatMap { it.vedlegg }
        ?.forEach {
          dokumentlagerService.hentFil(it.dokumentlagerDokumentId)?.let { fil ->
            zipArchive.putNextEntry(ZipEntry(fil.filnavn))
            zipArchive.write(fil.bytes)
            zipArchive.closeEntry()
          }
        }

    zipArchive.finish()
    zipArchive.close()
    bytebuffer.close()
    return bytebuffer.toByteArray()
  }

  private fun slaSammenTilJsonVedleggSpesifikasjon(
      ettersendelser: List<Ettersendelse>?,
      fiksDigisosId: String
  ): JsonVedleggSpesifikasjon? {
    val vedleggSpesifikasjoner =
        ettersendelser
            ?.map { dokumentlagerService.hentDokument(fiksDigisosId, it.vedleggMetadata) }
            ?.map { objectMapper.readValue(it, JsonVedleggSpesifikasjon::class.java) }

    return JsonVedleggSpesifikasjon().withVedlegg(vedleggSpesifikasjoner?.flatMap { it.vedlegg })
  }
}

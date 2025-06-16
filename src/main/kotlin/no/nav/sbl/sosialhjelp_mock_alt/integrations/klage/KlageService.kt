package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import org.springframework.stereotype.Service

@Service
class KlageService(
    private val soknadService: SoknadService,
    private val klageStorage: KlageStorage,
) {
  fun leggTilKlage(
      digisosId: UUID,
      kommunenummer: String,
      navEksternRefId: UUID,
      klageId: UUID,
      personId: String,
      klageJsonDokumentId: UUID,
      vedleggJsonDokumentId: UUID,
      vedleggPdfDokumentId: String,
      vedleggSpec: List<DigisosVedlegg>?
  ) {
    soknadService.hentSoknad(digisosId.toString()) ?: error("Finnes ingen søknad for DigisosId")

    if (!klageStorage.metadataExists(digisosId)) {
      klageStorage.createMetadata(
          DigisosKlagerMetadata(fiksDigisosId = digisosId, personId = personId))
    }

    klageStorage.addKlage(
        DigisosKlage(
            klageId = klageId,
            navEksternRefId = navEksternRefId,
            klageDokument = KlageDokument(filnavn = "et filnavn", storrelse = 0L),
            vedlegg = vedleggSpec ?: emptyList(),
            vedleggMetadata = vedleggJsonDokumentId,
            metadata = klageJsonDokumentId,
            sendtKvittering = SendtKvittering(DigisosSendtStatus("SENDT"), emptyList()),
            trukket = false,
        ))
  }

  fun hentAlleKlagerForPerson(personId: String): List<DigisosKlagerMetadata> =
      klageStorage.hentKlagerMetadataForPerson(personId)
}

data class KlageJson(
    val klageId: UUID,
    val navEksternRefId: UUID,
    val klageTekst: String,
)

@JvmInline value class FiksDigisosId(val value: String)

enum class KlageStatus {
  SENDT,
  MOTTATT,
  UNDER_BEHANDLING,
  FERDIG_BEHANDLET,
  HOS_STATSFORVALTER
}

enum class KlageUtfall {
  NYTT_VEDTAK,
  AVVIST,
}

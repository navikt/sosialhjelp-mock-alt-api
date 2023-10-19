package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonDokumentlagerFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonSvarUtFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.sosialhjelp_mock_alt.integrations.klage.InputKlage
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import org.springframework.stereotype.Service

@Service
class KlageService(private val soknadService: SoknadService) {
  private val klager: MutableMap<FiksDigisosId, MutableList<Klage>> = mutableMapOf()

  fun leggTilKlage(fiksDigisosId: FiksDigisosId, klage: InputKlage) {
    val soknad =
        soknadService.hentSoknad(fiksDigisosId.value)
            ?: error("Ingen søknad for fiksDigisosId: $fiksDigisosId")
    val jsonDigisosSoker: JsonDigisosSoker =
        soknadService.hentDokument(soknad.fiksDigisosId, soknad.digisosSoker!!.metadata)?.let {
          objectMapper.readValue(it)
        }
            ?: error("Fant ikke dokument med id ${soknad.digisosSoker!!.metadata}")
    val vedtak =
        jsonDigisosSoker.hendelser
            .asSequence()
            .filterIsInstance<JsonVedtakFattet>()
            .filter { it.utfall == JsonVedtakFattet.Utfall.INNVILGET }
            .map { it.vedtaksfil.referanse }
            .filter {
              when (it) {
                is JsonDokumentlagerFilreferanse -> it.id in klage.vedtaksIds
                is JsonSvarUtFilreferanse -> it.id in klage.vedtaksIds
                else -> error("Ukjent type på filreferanse")
              }
            }
            .map {
              when (it) {
                is JsonDokumentlagerFilreferanse -> it.id
                is JsonSvarUtFilreferanse -> it.id
                else -> error("Ukjent type på filreferanse")
              }
            }
            .toList()
    val dokumentLagerRef =
        soknadService.leggInnIDokumentlager("klage", objectMapper.writeValueAsBytes(klage))
    klager
        .getOrPut(fiksDigisosId) { mutableListOf() }
        .add(
            Klage(
                fiksDigisosId.value,
                dokumentLagerRef,
                vedtakRef = vedtak,
                status = KlageStatus.UNDER_BEHANDLING))
  }

  fun hentKlager(fiksDigisosId: FiksDigisosId): List<Klage> = klager[fiksDigisosId] ?: emptyList()
}

@JvmInline value class FiksDigisosId(val value: String)

data class Klage(
    val fiksDigisosId: String,
    val filRef: String,
    val vedtakRef: List<String>,
    val status: KlageStatus,
    val utfall: KlageUtfall? = null
)

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

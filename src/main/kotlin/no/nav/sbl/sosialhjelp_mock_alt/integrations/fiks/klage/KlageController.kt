package no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks.klage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.ks.fiks.io.client.FiksIOKlient
import no.ks.fiks.io.client.FiksIOKlientFactory
import no.ks.fiks.io.client.konfigurasjon.FiksIOKonfigurasjon
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.FiksDigisosId
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.Klage
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.KlageService
import no.nav.sbl.sosialhjelp_mock_alt.maskinporten.MaskinportenClient
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

private const val ORG_NR = "910229567"

@Service
@Profile("digisos-ekstern")
class KlageIO(
    fiksIOConfig: FiksIOKonfigurasjon,
    private val klageService: KlageService,
    private val maskinportenClient: MaskinportenClient,
) {

  private val log by logger()

  private val fiksIOKlient: FiksIOKlient by lazy {
    FiksIOKlientFactory(fiksIOConfig)
        .setMaskinportenAccessTokenSupplier {
          runBlocking { maskinportenClient.getToken() }
        }
        .build()
  }

  @EventListener(ApplicationReadyEvent::class)
  fun hentKlageListener() =
      runBlocking(Dispatchers.IO) {
        launch {
          fiksIOKlient.newSubscription { melding, svarSender ->
            if (melding.meldingType == "no.nav.sosialhjelp.klage.v1.hent") {
              runCatching {
                    melding.dekryptertZipStream.use { FiksDigisosId(String(it.readBytes())) }
                  }
                  .map {
                    val klager = klageService.hentKlager(it)
                    svarSender.svar(
                        melding.meldingType,
                        objectMapper.writeValueAsString(klager).byteInputStream(),
                        "???",
                        melding.klientMeldingId)
                  }
                  .onFailure {
                    log.error("Feil ved mottak av hent-melding", it)
                    svarSender.nackWithRequeue()
                  }
            } else {
              svarSender.nack()
            }
          }
        }
      }

  @EventListener(ApplicationReadyEvent::class)
  fun leverKlageListener() =
      runBlocking(Dispatchers.IO) {
        fiksIOKlient
            .lyttEtterKlager()
            .onEach { klageService.leggTilKlage(FiksDigisosId(it.fiksDigisosId), it) }
            .catch { log.error("Fikk feil fra klagelytter", it) }
            .collect()
      }

  fun FiksIOKlient.lyttEtterKlager() = callbackFlow {
    newSubscription { melding, svarSender ->
      if (melding.meldingType == "no.nav.sosialhjelp.klage.v1.send") {
        runCatching {
              melding.dekryptertZipStream.use {
                ObjectMapper().readValue<InputKlage>(String(it.readBytes()))
              }
            }
            .mapCatching {
              channel
                  .trySendBlocking(it)
                  .onSuccess {
                    svarSender.ack()
                    svarSender.svar(
                        "no.nav.kvittering", "dunno lol", "dunnolol.txt", melding.meldingId)
                  }
                  .onFailure { svarSender.nackWithRequeue() }
                  .onClosed { svarSender.nackWithRequeue() }
            }
            .onFailure {
              log.error("Fikk feil i mottak av klage", it)
              svarSender.nackWithRequeue()
            }
      } else {
        svarSender.nack()
      }
    }

    awaitClose { close() }
  }
}

@RestController
@Profile("!digisos-ekstern")
class KlageController(
    private val klageService: KlageService,
) {
  @GetMapping("/fiks/digisos/api/v1/{fiksDigisosId}/klage")
  fun getKlager(@PathVariable fiksDigisosId: FiksDigisosId): ResponseEntity<List<Klage>> {
    klageService.hentKlager(fiksDigisosId)
    return ResponseEntity.ok(klageService.hentKlager(fiksDigisosId))
  }

  @PostMapping("/fiks/digisos/api/v1/{fiksDigisosId}/klage")
  fun leggTilKlage(
      @PathVariable fiksDigisosId: FiksDigisosId,
      @RequestBody klage: InputKlage
  ): ResponseEntity<Unit> {
    klageService.leggTilKlage(fiksDigisosId, klage)
    return ResponseEntity.ok(Unit)
  }
}

data class InputKlage(
    val fiksDigisosId: String,
    val klageTekst: String,
    val vedtaksIds: List<String>,
    val vedlegg: List<MultipartFile> = emptyList()
)

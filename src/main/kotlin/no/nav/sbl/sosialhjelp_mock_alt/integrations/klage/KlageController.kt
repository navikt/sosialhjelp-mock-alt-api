package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.FiksDigisosId
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.Klage
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.KlageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

// @Service
// @Profile("digisos-ekstern")
// class KlageIO(
//    private val klageService: KlageService,
//    private val fiksIOKlient: FiksIOKlient,
// ) {
//
//  private val log by logger()
//
//  @EventListener(ApplicationReadyEvent::class)
//  fun hentKlageListener() =
//      runBlocking(Dispatchers.IO) {
//        launch {
//          fiksIOKlient.newSubscription { melding, svarSender ->
//            if (melding.meldingType == "no.nav.sosialhjelp.klage.v1.hent") {
//              runCatching {
//                    melding.dekryptertZipStream.use { FiksDigisosId(String(it.readBytes())) }
//                  }
//                  .map {
//                    val klager = klageService.hentKlager(it)
//                    svarSender.svar(
//                        "no.nav.sosialhjelp.klage.v1.hent",
//                        objectMapper.writeValueAsString(klager).byteInputStream(),
//                        "klager.json",
//                        melding.klientMeldingId)
//                  }
//                  .onFailure {
//                    log.error("Feil ved mottak av hent-melding", it)
//                    svarSender.nackWithRequeue()
//                  }
//            } else {
//              svarSender.nack()
//            }
//          }
//        }
//      }
//
//  @EventListener(ApplicationReadyEvent::class)
//  fun leverKlageListener() =
//      runBlocking(Dispatchers.IO) {
//        fiksIOKlient
//            .lyttEtterKlager()
//            .onEach { klageService.leggTilKlage(FiksDigisosId(it.fiksDigisosId), it) }
//            .catch { log.error("Fikk feil fra klagelytter", it) }
//            .collect()
//      }
//
//  fun FiksIOKlient.lyttEtterKlager() = callbackFlow {
//    newSubscription { melding, svarSender ->
//      if (melding.meldingType == "no.nav.sosialhjelp.klage.v1.send") {
//        runCatching {
//              melding.dekryptertZipStream.use {
//                ObjectMapper().readValue<InputKlage>(String(it.readBytes()))
//              }
//            }
//            .mapCatching {
//              channel
//                  .trySendBlocking(it)
//                  .onSuccess {
//                    svarSender.ack()
//                    svarSender.svar(
//                        "no.nav.sosialhjelp.klage.v1.send.kvittering",
//                        "dunno lol",
//                        "dunnolol.txt",
//                        melding.meldingId)
//                  }
//                  .onFailure { svarSender.nackWithRequeue() }
//                  .onClosed { svarSender.nackWithRequeue() }
//            }
//            .onFailure {
//              log.error("Fikk feil i mottak av klage", it)
//              svarSender.nackWithRequeue()
//            }
//      } else {
//        svarSender.nack()
//      }
//    }
//
//    awaitClose { close() }
//  }
// }

@RestController
class KlageController(
    private val klageService: KlageService,
) {
  @GetMapping("/klage/{fiksDigisosId}/klage")
  fun getKlager(@PathVariable fiksDigisosId: FiksDigisosId): ResponseEntity<List<Klage>> {
    klageService.hentKlager(fiksDigisosId)
    return ResponseEntity.ok(klageService.hentKlager(fiksDigisosId))
  }

  @PostMapping("/klage/{fiksDigisosId}/klage")
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

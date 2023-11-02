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

package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager

import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService.Companion.log
import org.springframework.stereotype.Service

@Service
class DokumentlagerService {

  private val fillager: FixedFileStorage = FixedFileStorage()
  private val dokumentLager: HashMap<String, String> = HashMap() // Lagres som rå json

  fun hentFil(dokumentlagerId: String): FileEntry? {
    log.debug("Henter fil med id: $dokumentlagerId")
    return fillager.find(dokumentlagerId)
  }

  fun lagreFil(vedleggId: String, filnavn: String, bytes: ByteArray) {
    log.info("Lagrer fil med id: $vedleggId og filnavn: $filnavn")
    fillager.add(vedleggId, filnavn, bytes)
  }

  fun hentDokument(digisosId: String?, dokumentlagerId: String): String? {
    log.debug("Henter dokument med id: $dokumentlagerId")
    return dokumentLager[dokumentlagerId] // Allerede lagret som json
  }

  fun leggTilDokument(dokumentlagerId: String, dokument: String) {
    dokumentLager[dokumentlagerId] = dokument
  }
}

class FixedFileStorage {
  private val items: MutableList<FileEntry> = mutableListOf()

  private val maxSize = 200

  fun add(key: String, fileName: String, bytes: ByteArray) {
    while (items.size >= maxSize) {
      items.removeAt(0)
    }
    items.add(FileEntry(key, fileName, bytes))
  }

  fun find(key: String): FileEntry? {
    return items.findLast { it.key == key }
  }
}

class FileEntry(val key: String, val filnavn: String, val bytes: ByteArray)

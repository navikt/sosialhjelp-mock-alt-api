package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager

import java.util.UUID
import org.springframework.stereotype.Component

@Component
class Dokumentlager {
  fun save(dokumentlagerId: String, data: Any) {
    storage[dokumentlagerId] = data
  }

  fun save(data: Any): UUID = UUID.randomUUID().also { storage[it.toString()] = data }

  fun get(dokumentlagerId: String): Any? = storage[dokumentlagerId]

  fun get(dokumentlagerId: UUID): Any? = get(dokumentlagerId.toString())

  fun getAll(): Map<String, Any> = storage

  companion object {
    private val storage: HashMap<String, Any> = HashMap()
  }
}

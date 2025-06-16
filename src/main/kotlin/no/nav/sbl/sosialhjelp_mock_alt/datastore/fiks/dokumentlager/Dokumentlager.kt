package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager

import java.util.UUID
import org.springframework.stereotype.Component

@Component
class Dokumentlager {
    fun save(dokumentlagerId: String, json: String) {
        storage[dokumentlagerId] = json
    }

    fun save(json: String): UUID = UUID.randomUUID().also { storage[it.toString()] = json }

    fun get(dokumentlagerId: String): String? = storage[dokumentlagerId]

    fun get(dokumentlagerId: UUID): String? = get(dokumentlagerId.toString())

    companion object {
        private val storage: HashMap<String, String> = HashMap()
    }
}
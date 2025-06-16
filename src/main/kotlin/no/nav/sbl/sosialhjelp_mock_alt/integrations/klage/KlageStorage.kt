package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import org.springframework.stereotype.Component

@Component
class KlageStorage {

    fun metadataExists(digisosId: UUID): Boolean = storage.any { it.fiksDigisosId == digisosId }

    fun addKlage(klage: DigisosKlage) {


    }

    fun createMetadata(klagerMetadata: DigisosKlagerMetadata) {
        storage.find { it.fiksDigisosId == klagerMetadata.fiksDigisosId } ?: storage.add(klagerMetadata)
    }

    fun hentKlagerMetadataForPerson(personId: String): List<DigisosKlagerMetadata> =
        storage.filter { it.personId == personId }

    companion object {
        private val storage: MutableSet<DigisosKlagerMetadata> = mutableSetOf()
    }
}
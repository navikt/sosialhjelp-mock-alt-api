package no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.mellomlagring

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MellomlagringServiceTest {
    @Test
    fun lagreFil_generererUnikFilIdPerOpplasting() {
        val service = MellomlagringService()
        val navEksternRefId = "sak-123"

        val filId1 =
            service.lagreFil(
                navEksternRefId = navEksternRefId,
                filnavn = "bilde1.jpg",
                bytes = "a".toByteArray(),
                mimeType = "image/jpeg",
            )
        val filId2 =
            service.lagreFil(
                navEksternRefId = navEksternRefId,
                filnavn = "bilde2.png",
                bytes = "b".toByteArray(),
                mimeType = "image/png",
            )

        assertNotEquals(filId1, filId2)

        val metadata = service.getAll(navEksternRefId)?.mellomlagringMetadataList.orEmpty()
        assertEquals(2, metadata.size)
        assertTrue(metadata.any { it.filId == filId1 && it.filnavn == "bilde1.jpg" })
        assertTrue(metadata.any { it.filId == filId2 && it.filnavn == "bilde2.png" })
    }
}

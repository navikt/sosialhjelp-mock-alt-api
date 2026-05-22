package no.nav.sbl.sosialhjelp.mock.alt.integrations.fiks.mellomlagring

import no.nav.sbl.sosialhjelp.mock.alt.datastore.feil.FeilService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.mellomlagring.MellomlagringService
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest

internal class FiksMellomlagringControllerTest {
    @Test
    fun postMellomlagretVedlegg_brukerRiktigFilVedDuplikateFilnavnOgFaktiskStorrelse() {
        val service = MellomlagringService()
        val controller = FiksMellomlagringController(FeilService(), service)
        val navEksternRefId = "sak-123"

        val file1 = MockMultipartFile("files", "vedlegg.png", "image/png", byteArrayOf(1))
        val file2 = MockMultipartFile("files", "vedlegg.png", "image/png", byteArrayOf(2, 3))

        val request = MockMultipartHttpServletRequest()
        request.addParameter(
            "metadata",
            objectMapper.writeValueAsString(FilMetadata("vedlegg.png", "image/png", 999L)),
            objectMapper.writeValueAsString(FilMetadata("vedlegg.png", "image/png", 999L)),
        )

        val response =
            controller.postMellomlagretVedlegg(
                headers = HttpHeaders(),
                navEksternRefId = navEksternRefId,
                files = listOf(file1, file2),
                request = request,
            )

        assertEquals(HttpStatus.OK, response.statusCode)

        val metadataList = (response.body as MellomlagringDto).mellomlagringMetadataList.orEmpty()
        assertEquals(2, metadataList.size)
        assertNotEquals(metadataList[0].filId, metadataList[1].filId)
        assertEquals(1L, metadataList[0].storrelse)
        assertEquals(2L, metadataList[1].storrelse)

        assertArrayEquals(byteArrayOf(1), service.get(navEksternRefId, metadataList[0].filId))
        assertArrayEquals(byteArrayOf(2, 3), service.get(navEksternRefId, metadataList[1].filId))
    }
}

package no.nav.sbl.sosialhjelp.mock.alt.integrations.fiks

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.dokumentlager.DokumentlagerService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.model.DigisosApiWrapper
import no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.model.SakWrapper
import no.nav.sbl.sosialhjelp.mock.alt.objectMapper
import no.nav.sbl.sosialhjelp.mock.alt.utils.FAST_FNR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID

@SpringBootTest(properties = ["mock-oauth2-server.port=0"])
class FiksBulkDokumentControllerTest
    @Autowired
    constructor(
        private val context: WebApplicationContext,
        private val soknadService: SoknadService,
        private val dokumentlagerService: DokumentlagerService,
    ) {
        @Test
        fun `bulk request returns both documents as named JSON parts`() {
            val documents = listOf(opprettSak(), opprettSak())
            val mvc = MockMvcBuilders.webAppContextSetup(context).build()
            val response =
                mvc
                    .perform(
                        post("/fiks/digisos/api/v1/soknader/dokumenter")
                            .header("nav-personident", FAST_FNR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(BulkDokumenterRequest(documents))),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response

            val mediaType = MediaType.parseMediaType(response.contentType!!)
            assertEquals("multipart", mediaType.type)
            assertEquals("mixed", mediaType.subtype)
            val boundary = mediaType.getParameter("boundary")
            assertNotNull(boundary)
            val parts = response.contentAsString.split("--$boundary\r\n").drop(1)
            assertEquals(2, parts.size)
            documents.zip(parts).forEach { (document, part) ->
                val (headers, content) = part.split("\r\n\r\n", limit = 2)
                assertEquals(
                    "${document.digisosId}_${document.dokumentlagerId}",
                    ContentDisposition.parse(headers.lineSequence().first().substringAfter(": ")).name,
                )
                val contentType = MediaType.parseMediaType(headers.lineSequence().last().substringAfter(": "))
                assertEquals(MediaType.APPLICATION_JSON.type, contentType.type)
                assertEquals(MediaType.APPLICATION_JSON.subtype, contentType.subtype)
                val soker = objectMapper.readValue(content.substringBefore("\r\n--$boundary").trimEnd(), JsonDigisosSoker::class.java)
                assertEquals("default", soker.avsender.systemnavn)
                assertEquals(1, soker.hendelser.size)
            }
        }

        @Test
        fun `bulk request rejects missing and mismatched documents`() {
            val first = opprettSak()
            val second = opprettSak()
            val mvc = MockMvcBuilders.webAppContextSetup(context).build()
            val invalidDocuments =
                listOf(
                    BulkDokumentRequest(first.digisosId, second.dokumentlagerId),
                    BulkDokumentRequest(UUID.randomUUID().toString(), first.dokumentlagerId),
                    BulkDokumentRequest(first.digisosId, UUID.randomUUID().toString()),
                )
            invalidDocuments.forEach { document ->
                mvc
                    .perform(
                        post("/fiks/digisos/api/v1/soknader/dokumenter")
                            .header("nav-personident", FAST_FNR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(BulkDokumenterRequest(listOf(document)))),
                    ).andExpect(status().isNotFound)
            }
            mvc
                .perform(
                    post("/fiks/digisos/api/v1/soknader/dokumenter")
                        .header("nav-personident", "12345678901")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BulkDokumenterRequest(listOf(first)))),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `bulk request rejects unsafe part names and returns empty multipart for an empty list`() {
            val mvc = MockMvcBuilders.webAppContextSetup(context).build()
            mvc
                .perform(
                    post("/fiks/digisos/api/v1/soknader/dokumenter")
                        .header("nav-personident", FAST_FNR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BulkDokumenterRequest(listOf(BulkDokumentRequest("bad\r\nid", "id"))))),
                ).andExpect(status().isBadRequest)

            val response =
                mvc
                    .perform(
                        post("/fiks/digisos/api/v1/soknader/dokumenter")
                            .header("nav-personident", FAST_FNR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"dokumenter\":[]}"),
                    ).andExpect(status().isOk)
                    .andReturn()
                    .response
            val boundary = MediaType.parseMediaType(response.contentType!!).getParameter("boundary")
            assertEquals("--$boundary--\r\n", response.contentAsString)
        }

        @Test
        fun `case update route still accepts a digisos wrapper`() {
            val document = opprettSak()
            val json = dokumentlagerService.hentDokument(document.digisosId, document.dokumentlagerId)!!
            val soker = objectMapper.readValue(json, JsonDigisosSoker::class.java)
            val body = objectMapper.writeValueAsString(DigisosApiWrapper(SakWrapper(soker), "type"))

            MockMvcBuilders
                .webAppContextSetup(context)
                .build()
                .perform(
                    post("/fiks/digisos/api/v1/org/${document.digisosId}")
                        .header("nav-personident", FAST_FNR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isOk)
        }

        private fun opprettSak(): BulkDokumentRequest {
            val id = UUID.randomUUID().toString()
            soknadService.opprettDigisosSak("org", "0301", FAST_FNR, id)
            val metadata = soknadService.hentSoknad(id)!!.digisosSoker!!.metadata
            return BulkDokumentRequest(id, metadata)
        }
    }

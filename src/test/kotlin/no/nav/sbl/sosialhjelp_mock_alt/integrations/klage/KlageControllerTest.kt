package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import com.fasterxml.jackson.core.type.TypeReference
import java.io.File
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelp_mock_alt.ExampleFilesRepository.PDF_FILE
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.FixedFileStorage
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.SoknadService
import no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.dokumentlager.Dokumentlager
import no.nav.sbl.sosialhjelp_mock_alt.integrations.fiks.mellomlagring.FiksMellomlagringController.FilMetadata
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36000S")
class KlageControllerTest {

  @LocalServerPort
  private var port: Int = 0
  private lateinit var restClient: RestClient

  @Autowired
  private lateinit var fillager: FixedFileStorage

  @Autowired
  private lateinit var dokumentlager: Dokumentlager

  @Autowired
  private lateinit var soknadService: SoknadService

  @BeforeEach
  fun setup() {
    restClient = RestClient.create("http://localhost:$port/sosialhjelp/mock-alt-api")
  }

  @Test
  fun `Skal kunne hente opp igjen sendt klage`() {
    createDigisosSoker()

    val klageJson = createKlageJson("Jeg klager på noe")
      .let { objectMapper.writeValueAsString(it) }

    lastOppTilMellomlager("Fil1.pdf")
    lastOppTilMellomlager("Fil2.pdf")

    val vedleggJson = createVedleggSpecJson(listOf("Fil1.pdf", "Fil2.pdf"))
      .let { objectMapper.writeValueAsString(it) }

    val body = LinkedMultiValueMap<String, Any>()
    body.add("klageJson", createEntityFromJson(klageJson))
    body.add("vedleggJson", createEntityFromJson(vedleggJson))

    body.add("klagePdf", createEntityFromFile(PDF_FILE, MediaType.APPLICATION_OCTET_STREAM, "klagePdf"))

    restClient.post()
      .uri(sendUrl(UUID.randomUUID()))
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .body(body)
      .retrieve()
      .toBodilessEntity()

    val bodyString = restClient.get()
      .uri(hentUrl)
      .retrieve()
      .body(String::class.java)

    val digisosKlagerMetadatas = objectMapper.readValue(bodyString, object : TypeReference<List<DigisosKlagerMetadata>>() {})
    val klagerMetadata = digisosKlagerMetadatas.firstOrNull() ?: error("Metadata-liste finnes ikke.")

    val klage = klagerMetadata.klager.firstOrNull() ?: error("Finnes ingen klager")
    assertThat(dokumentlager.get(klage.metadata)).isNotNull()
    assertThat(dokumentlager.get(klage.vedleggMetadata)).isNotNull()
    assertThat(fillager.find(klage.klageDokument.dokumenlagerDokumentId.toString())).isNotNull()
    assertThat(klage.vedlegg.map { dokumentlager.get(it.dokumentlagerDokumentId) }).isNotNull()
  }

  @Test
  fun `Klage uten obligatoriske filer skal gi feil`() {
    createDigisosSoker()

    val body = LinkedMultiValueMap<String, Any>()

    // sender ikke med noen filer

    restClient.post()
      .uri(sendUrl(UUID.randomUUID()))
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .body(body)
      .exchange { _, response -> assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) }
  }

  private fun lastOppTilMellomlager(filnavn: String) {
    val body = LinkedMultiValueMap<String, Any>()

    body.add(filnavn, createEntityFromFile(PDF_FILE, MediaType.APPLICATION_OCTET_STREAM, filnavn))
    FilMetadata(
      filnavn = filnavn,
      mimetype = MediaType.APPLICATION_PDF_VALUE,
      storrelse = PDF_FILE.readBytes().size.toLong(),
    )
      .let { objectMapper.writeValueAsString(it) }
      .also { body.add("metadata", it) }

    restClient.post()
      .uri(mellomlagerUrl)
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .body(body)
      .retrieve()
      .toBodilessEntity()
  }

  private fun createDigisosSoker() {
    soknadService.opprettDigisosSak("12345678", "0302", "12345612345", digisosId.toString())
  }

  companion object {

    private val navEksternRefId: UUID = UUID.randomUUID()
    private val digisosId: UUID = UUID.randomUUID()
    private val kommunenummer: String = "0302"

    private val baseUrl = "/digisos/klage/api/v1/$digisosId/$kommunenummer/$navEksternRefId/"

    private fun sendUrl(klageId: UUID) = "$baseUrl/$klageId"
    private val hentUrl = "$baseUrl/klager"

    private val mellomlagerUrl = "/fiks/digisos/api/v1/mellomlagring/$navEksternRefId"
  }
}

private fun createEntityFromJson(json: String): HttpEntity<ByteArrayResource> {
//  val headerMap = LinkedMultiValueMap<String, String>()
//  headerMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
  return HttpEntity(
    ByteArrayResource(json.toByteArray()),
    HttpHeaders().apply { contentType =  MediaType.APPLICATION_JSON}
  )
}

private fun createEntityFromFile(file: File, contentType: MediaType, name: String): HttpEntity<ByteArrayResource> {

  val headerMap = LinkedMultiValueMap<String, String>()

  ContentDisposition.builder("form-data")
    .name(name)
    .filename(file.name)
    .build()
    .also { headerMap.add(HttpHeaders.CONTENT_DISPOSITION, it.toString()) }

  headerMap.add(HttpHeaders.CONTENT_TYPE, contentType.toString())

  return HttpEntity(ByteArrayResource(file.readBytes()), headerMap)
}

private fun createKlageJson(tekst: String) = KlageJson(
  klageId = UUID.randomUUID(),
  navEksternRefId = UUID.randomUUID(),
  vedtakId = UUID.randomUUID(),
  klageTekst = tekst
)

private fun createVedleggSpecJson(filnavnList: List<String>) = JsonVedleggSpesifikasjon()
  .withVedlegg(
    listOf(
      JsonVedlegg()
        .withType("Klage")
        .withTilleggsinfo("VedleggKlage")
        .withFiler(filnavnList.map { JsonFiler().withFilnavn(it) })
    )
  )


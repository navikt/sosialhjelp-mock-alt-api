package no.nav.sbl.sosialhjelp_mock_alt.integrations.klage

import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36000S")
class KlageControllerTest {

  @Autowired private lateinit var webTestClient: WebTestClient

  companion object {
    private val restClient: RestClient = RestClient.create()

    private val navEksternRefId: UUID = UUID.randomUUID()
    private val digisosId: UUID = UUID.randomUUID()
    private val kommunenummer: String = "0302"

    private val baseUrl = "/digisos/klage/api/v1/$digisosId/$kommunenummer/$navEksternRefId/"

    private fun sendUrl(klageId: UUID) = "$baseUrl/$klageId"
  }
}

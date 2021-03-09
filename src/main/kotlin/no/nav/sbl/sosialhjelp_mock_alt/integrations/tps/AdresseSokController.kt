package no.nav.sbl.sosialhjelp_mock_alt.integrations.tps


import no.nav.sbl.sosialhjelp_mock_alt.datastore.adresse.AdresseService
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdresseSokController(private val adresseService: AdresseService) {

    @GetMapping("/tps/adressesoek", produces = ["application/json;charset=UTF-8"])
    fun adressesok(@RequestParam parameters: MultiValueMap<String, String>): String {
        val postnummer = parameters["postnr"]
        return objectMapper.writeValueAsString(adresseService.getAdresseInfo(postnummer!![0]))
    }

}

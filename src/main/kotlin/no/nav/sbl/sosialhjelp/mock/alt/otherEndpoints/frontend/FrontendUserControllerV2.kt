package no.nav.sbl.sosialhjelp.mock.alt.otherEndpoints.frontend

import jakarta.validation.Valid
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.PdlService
import no.nav.sbl.sosialhjelp.mock.alt.datastore.pdl.model.Personalia
import no.nav.sbl.sosialhjelp.mock.alt.otherEndpoints.frontend.model.FrontendPersonalia
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/mock-alt/v2/brukere/")
class FrontendUserControllerV2(
    private val pdlService: PdlService,
    private val mockBrukerService: MockBrukerService,
) {
    @PostMapping
    fun postMockPersonV2(
        @Valid @RequestBody personalia: FrontendPersonalia,
    ): ResponseEntity<Unit> {
        mockBrukerService.newPerson(personalia)
        return ResponseEntity.created(URI(personalia.fnr)).build()
    }

    @GetMapping fun listMockPersonerV2(): Collection<Personalia> = pdlService.getPersonListe()

    @GetMapping("{ident}")
    fun getMockPersonV2(
        @PathVariable ident: String,
    ): FrontendPersonalia = mockBrukerService.getPerson(ident)
}

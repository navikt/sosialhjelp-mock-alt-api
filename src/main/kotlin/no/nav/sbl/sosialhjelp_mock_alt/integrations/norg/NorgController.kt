package no.nav.sbl.sosialhjelp_mock_alt.integrations.norg

import no.nav.sbl.sosialhjelp_mock_alt.integrations.norg.model.NavEnhet
import no.nav.sbl.sosialhjelp_mock_alt.objectMapper
import no.nav.sbl.sosialhjelp_mock_alt.utils.logger
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NorgController {
    companion object {
        val log by logger()
    }

    @RequestMapping("/norg_endpoint_url/enhet/{enhetsnr}")
    fun getToken(@PathVariable enhetsnr: String): String {
        val navEnhet = NavEnhet(
                enhetId = 198723989,
                navn = "NavEnehetsNavn",
                enhetNr = enhetsnr,
                status = "20",
                antallRessurser = 25,
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo
        )
        log.info("Henter nav enhet for id: $enhetsnr")
        return objectMapper.writeValueAsString(navEnhet)
    }

    @RequestMapping("/norg_endpoint_url/kodeverk/EnhetstyperNorg")
    fun hentEnhetstyperDummy(): String {
        log.info("Henter EnhetstyperNorg")
        return "OK"
    }

    private val sosialetjenesterInfo: String = """
        Til saksbehandler:
        Flere vakttelefoner:
        Mottak (nye brukere): 11112222
        Ungdom (18-24år, samt OT-ungdom 16år+): 11112222
        Avklaring (enslige over 20år u/hovedutford. rus/psyk.): 11112222
        Rus/psyk. (m/hovedutford. rus/psyk.): 11112222
        Familie (har barn som bor minst 50% hos bruker): 11112222
        Boligkontor: 11112222
        KVP: 11112222
        Intro. (har stønaden): 11112222
        Sosialfaglige tjenester: Boligkontor (Startlån, bostøtte, kommunal bostøtte, bolig for vanskeligstilte, kommunalt frikort, OT (oppfølgingstenesten), flyktningtjenesten, rus

        Sender post digitalt

        Digital søknad på nav.no/sosialhjelp. Dokumentasjon av vilkår kan ettersendes digitalt. Papir søknadsskjema på kommunens nettside og i V/P - Nye søkere: Ønsker kontakt før innsending av digital søknad - Ønsker kontakt i forkant før søknad om nødhjelp (mat/bolig)

        Saksbehandlingstider: Økonomisk sosialhjelp: 14 dager Startlån: 1mnd.
        Utbetalinger:
        Fast utbetalingsdato: 27-30 i mnd
        Siste tidspunkt for kjøring: 1030
        Utbetaling når utbetaling havner på helg/helligdag: siste virkedag før Utbetalingsmåter for nødhjelp: kronekort/rekvisisjon Kvalifiseringsstønad og introduksjonsstønad: 28 i mnd
    """.trimIndent()
}
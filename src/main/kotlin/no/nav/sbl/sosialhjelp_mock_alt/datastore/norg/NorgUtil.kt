package no.nav.sbl.sosialhjelp_mock_alt.datastore.norg

import no.nav.sbl.sosialhjelp_mock_alt.datastore.norg.model.NavEnhet
import no.nav.sbl.sosialhjelp_mock_alt.utils.randomInt

private val sosialetjenesterInfo: String =
    """
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
"""
        .trimIndent()

fun lagMockNavEnhet(enhetsnr: String, navn: String, enhetId: Int = randomInt(8)): NavEnhet {
  return NavEnhet(
      enhetId = enhetId,
      navn = navn,
      enhetNr = enhetsnr,
      status = "20",
      antallRessurser = 25,
      aktiveringsdato = "1999-10-10",
      nedleggelsesdato = "null",
      sosialeTjenester = sosialetjenesterInfo)
}

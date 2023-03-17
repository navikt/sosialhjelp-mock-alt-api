package no.nav.sbl.sosialhjelp_mock_alt.datastore.fiks.model

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning

fun defaultJsonSoknad(fiksDigisosId: String): JsonSoknad {
  return JsonSoknad()
      .withVersion("1.0.0")
      .withData(
          JsonData()
              .withPersonalia(
                  JsonPersonalia()
                      .withPersonIdentifikator(
                          JsonPersonIdentifikator()
                              .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                              .withVerdi(fiksDigisosId))
                      .withNavn(
                          JsonSokernavn()
                              .withKilde(JsonSokernavn.Kilde.SYSTEM)
                              .withFornavn("")
                              .withMellomnavn("")
                              .withEtternavn(""))
                      .withKontonummer(JsonKontonummer().withKilde(JsonKilde.BRUKER)))
              .withArbeid(JsonArbeid())
              .withUtdanning(JsonUtdanning().withKilde(JsonKilde.BRUKER))
              .withFamilie(JsonFamilie().withForsorgerplikt(JsonForsorgerplikt()))
              .withBegrunnelse(
                  JsonBegrunnelse()
                      .withKilde(JsonKildeBruker.BRUKER)
                      .withHvorforSoke("")
                      .withHvaSokesOm(""))
              .withBosituasjon(JsonBosituasjon().withKilde(JsonKildeBruker.BRUKER))
              .withOkonomi(
                  JsonOkonomi()
                      .withOpplysninger(
                          JsonOkonomiopplysninger()
                              .withUtbetaling(emptyList())
                              .withUtgift(emptyList()))
                      .withOversikt(
                          JsonOkonomioversikt()
                              .withInntekt(emptyList())
                              .withUtgift(emptyList())
                              .withFormue(emptyList()))))
      .withMottaker(
          JsonSoknadsmottaker()
              .withNavEnhetsnavn("Mock bydel, mock kommune")
              .withEnhetsnummer("0301")
              .withKommunenummer("1337"))
      .withDriftsinformasjon(JsonDriftsinformasjon())
      .withKompatibilitet(emptyList())
}

package no.nav.sbl.sosialhjelp.mock.alt.datastore.fiks.model

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

fun defaultJsonSoknad(fiksDigisosId: String): JsonSoknad =
    JsonSoknad(
        version = "1.0.0",
        data =
            JsonData(
                personalia =
                    JsonPersonalia(
                        personIdentifikator = JsonPersonIdentifikator(JsonPersonIdentifikator.Kilde.SYSTEM, fiksDigisosId),
                        navn = JsonSokernavn(JsonSokernavn.Kilde.SYSTEM, "", "", ""),
                        kontonummer = JsonKontonummer(kilde = JsonKilde.BRUKER),
                    ),
                begrunnelse = JsonBegrunnelse(JsonKildeBruker.BRUKER, "Livsopphold", ""),
                okonomi =
                    JsonOkonomi(
                        opplysninger = JsonOkonomiopplysninger(utbetaling = emptyList(), utgift = emptyList()),
                        oversikt = JsonOkonomioversikt(inntekt = emptyList(), utgift = emptyList(), formue = emptyList()),
                    ),
                arbeid = JsonArbeid(),
                utdanning = JsonUtdanning(kilde = JsonKilde.BRUKER),
                familie = JsonFamilie(forsorgerplikt = JsonForsorgerplikt()),
                bosituasjon = JsonBosituasjon(kilde = JsonKildeBruker.BRUKER),
            ),
        mottaker = JsonSoknadsmottaker(kommunenummer = "1337", enhetsnummer = "0301", navEnhetsnavn = "Mock bydel, mock kommune"),
        driftsinformasjon = JsonDriftsinformasjon(inntektFraSkatteetatenFeilet = false),
        kompatibilitet = emptyList(),
    )

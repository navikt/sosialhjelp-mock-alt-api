# sosialhjelp-mock-alt-api
En mock-tjeneste for DIGISOS.

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

## Bygging av docker image lokalt

Ved bygging av docker image lokalt (i motsetning til å bygge på vertsmaskin, eller i CI/CD) må du mappe inn din ~/.gradle/gradle.settings, slik:

```docker build --secret id=github_token,src=$HOME/.gradle/gradle.properties .```

## Teknologi
* Kotlin
* JDK 17
* Gradle
* Spring-boot
* navikt/token-support

### Krav
- JDK 17

## Hvordan komme i gang
### [Felles dokumentasjon for våre backend apper](https://teamdigisos.intern.nav.no/docs/utviklerdokumentasjon/kom%20igang%20med%20utvikling#backend-gradle)

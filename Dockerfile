FROM --platform=linux/amd64 navikt/java:11

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY build/libs/*.jar app.jar
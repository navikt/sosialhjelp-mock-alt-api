FROM gcr.io/distroless/java21-debian12

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY /build/libs/app.jar app.jar

ENTRYPOINT ["app.jar"]

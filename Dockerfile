FROM gcr.io/distroless/java21-debian12

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY build/libs/app.jar app.jar

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh

CMD ["app.jar"]
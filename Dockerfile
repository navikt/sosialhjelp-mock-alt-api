# syntax=docker/dockerfile:1.3
FROM gradle:jdk21 AS builder

# Set the working directory in the Docker image filesystem.
WORKDIR /home/gradle/project

# Grant write permission to the Gradle user for the project directory
USER root
RUN chown -R gradle /home/gradle/project
USER gradle

# Copy the Gradle build file(s) and source code into the image
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts /home/gradle/project/
COPY --chown=gradle:gradle src /home/gradle/project/src
COPY --chown=gradle:gradle gradle /home/gradle/project/gradle

# Build the application
# --no-daemon is used for better Docker compatibility and resource usage
RUN --mount=type=secret,id=github_token,dst=/home/gradle/.gradle/gradle.properties,required=true,uid=1000 \
    gradle build \
    --no-daemon -x test

FROM gcr.io/distroless/java21-debian12

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY --from=builder /home/gradle/project/build/libs/app.jar app.jar

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"

CMD ["app.jar"]

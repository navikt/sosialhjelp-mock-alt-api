import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `jvm-test-suite`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.versions)
    alias(libs.plugins.spotless)
}

group = "no.nav.sbl"

version = "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_21

val githubUser: String? by project
val githubPassword: String? by project

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/navikt/*")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

configurations { testImplementation { exclude(group = "org.mockito") } }

dependencies {
    constraints {
        implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.22") {
            because("Temporary fix: upgrades embedded Tomcat from 11.0.21 to 11.0.22 to address CVE-2026-41293, CVE-2026-43512, CVE-2026-43513, CVE-2026-43515, and CVE-2026-42498 until Spring Boot ships the fix")
        }
        implementation("org.apache.tomcat.embed:tomcat-embed-el:11.0.22") {
            because("Keeps Tomcat modules aligned with the tomcat-embed-core security fix")
        }
        implementation("org.apache.tomcat.embed:tomcat-embed-websocket:11.0.22") {
            because("Keeps Tomcat modules aligned with the tomcat-embed-core security fix")
        }
        implementation("io.netty:netty-codec-http:4.2.15.Final") {
            because("Fixes high-severity Netty HTTP request smuggling and HttpClientCodec response desynchronization vulnerabilities")
        }
        implementation("io.netty:netty-codec-compression:4.2.15.Final") {
            because("Fixes high-severity Netty Lz4FrameDecoder resource exhaustion vulnerability")
        }
        implementation("org.bouncycastle:bcprov-jdk18on:1.84") {
            because("Fixes high-severity Bouncy Castle timing side-channel vulnerability")
        }
        implementation("org.bouncycastle:bcpkix-jdk18on:1.84") {
            because("Keeps Bouncy Castle modules aligned with the bcprov security fix")
        }
        implementation("org.bouncycastle:bcutil-jdk18on:1.84") {
            because("Keeps Bouncy Castle modules aligned with the bcprov security fix")
        }
    }

    implementation(kotlin("reflect"))

    implementation(libs.bundles.spring.boot)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.sosialhjelp.common.api)
    implementation(libs.soknadsosialhjelp.filformat)
    implementation(libs.token.validation.spring)
    implementation(libs.token.validation.spring.test)
    implementation(libs.mock.oauth2.server)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.springdoc.openapi.starter.common)

    // Logback
    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder)

    // Opentelemetry
    implementation(platform(libs.opentelemetry.bom))
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.instrumentation.logback)

    testImplementation(libs.spring.boot.starter.test)
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf { candidate.version.isNonStable() && !currentVersion.isNonStable() }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

spotless {
    format("misc") {
        target("*.md", ".gitignore", "Dockerfile")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin { ktlint(libs.versions.ktlint.get()) }
    kotlinGradle { ktlint(libs.versions.ktlint.get()) }
}

val installHook = tasks.getByName<com.diffplug.gradle.spotless.SpotlessInstallPrePushHookTask>("spotlessInstallGitPrePushHook")

tasks.assemble.get().dependsOn(installHook)

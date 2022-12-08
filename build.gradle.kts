import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "3.0.0"
val coroutinesVersion = "1.6.4"
val sosialhjelpCommonVersion = "1.20221214.0749-b633a3e"
val filformatVersion = "1.2022.11.16-08.18-c36037400819"
val tokenValidationVersion = "3.0.0"
val jacksonVersion = "2.14.1"
val springdocversion = "1.6.12"
val jsonSmartVersion = "2.4.8"
val mockOauth2ServerVersion = "0.5.6"
val junitVersion = "4.13.2"
val log4jVersion = "2.19.0"
val snakeyamlVersion = "1.33"
val ktlint = "0.45.2"
val svarUtVersion = "1.1.0"

val jakartaActivationApiVersion = "2.1.0"
val jakartaAnnotationApiVersion = "2.1.1"
val jakartaXmlBindApiVersion = "4.0.0"
val jakartaValidationApiVersion = "3.0.2"

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    id("org.springframework.boot") version "3.0.0"
    id("com.github.ben-manes.versions") version "0.44.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "no.nav.sbl"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

ktlint {
    this.version.set(ktlint)
}

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/navikt/sosialhjelp-common")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

configurations {
    "implementation" {
        exclude(group = "javax.activation", module = "activation")
        exclude(group = "javax.mail", module = "mailapi")
        exclude(group = "javax.validation", module = "validation-api")
    }
    "testImplementation" {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
        exclude(group = "org.mockito", module = "mockito-junit-jupiter")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("no.nav.sosialhjelp:sosialhjelp-common-api:$sosialhjelpCommonVersion")
    implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:$filformatVersion")
    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-validation-spring-test:$tokenValidationVersion")
    implementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")

    implementation("org.springdoc:springdoc-openapi-ui:$springdocversion")

    // SvarUt
    implementation("no.ks.fiks.svarut:svarut-rest-klient:$svarUtVersion")

    testImplementation("no.nav.security:token-validation-spring-test:$tokenValidationVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")

    //    spesifikke versjoner oppgradert etter ønske fra snyk
    constraints {
        implementation("net.minidev:json-smart:$jsonSmartVersion") {
            because("Snyk ønsker versjon 2.4.5 eller høyere")
        }

        implementation("junit:junit:$junitVersion") {
            because("Snyk ønsker versjon 4.13.1 eller høyere")
        }

        implementation("org.apache.logging.log4j:log4j-api:$log4jVersion") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.apache.logging.log4j:log4j-to-slf4j:$log4jVersion") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.yaml:snakeyaml:$snakeyamlVersion") {
            because("Snyk ønsker versjon 1.31 eller høyere")
        }

        // spring boot 3.0.0 -> jakarta
        implementation("jakarta.activation:jakarta.activation-api:$jakartaActivationApiVersion")
        implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")
        implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
        implementation("jakarta.xml.bind:jakarta.xml.bind-api:$jakartaXmlBindApiVersion")
    }
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && !currentVersion.isNonStable()
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "2.7.1"
val coroutinesVersion = "1.6.3"
val sosialhjelpCommonVersion = "1.20220629.1332-dfb4541"
val filformatVersion = "1.2022.04.29-13.11-459bee049a7a"
val tokenValidationVersion = "2.1.1"
val jacksonVersion = "2.13.3"
val springdocversion = "1.6.9"
val jsonSmartVersion = "2.4.8"
val mockOauth2ServerVersion = "0.5.1"
val junitVersion = "4.13.2"
val log4jVersion = "2.17.2"
val ktlint = "0.45.2"

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.spring") version "1.7.0"
    id("org.springframework.boot") version "2.7.1"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
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

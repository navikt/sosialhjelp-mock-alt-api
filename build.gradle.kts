import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "2.5.1"
val coroutinesVersion = "1.5.0"
val sosialhjelpCommonVersion = "1.05daec2"
val filformatVersion = "1.2021.04.15-10.42-6eb47b47da27"
val tokenValidationVersion = "1.3.8"
val jacksonVersion = "2.12.3"
val springdocversion = "1.5.7"
val jsonSmartVersion = "2.4.7"
val mockOauth2ServerVersion = "0.3.4"

plugins {
    application
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.ben-manes.versions") version "0.38.0"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

application {
    applicationName = "sosialhjelp-mock-alt-api"
    mainClass.set("no.nav.sbl.sosialhjelp_mock_alt.MockAltApplicationKt")
}

group = "no.nav.sbl"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

ktlint {
    this.version.set("0.41.0")
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

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")

    //    spesifikke versjoner oppgradert etter ønske fra snyk
    constraints {
        implementation("net.minidev:json-smart:$jsonSmartVersion") {
            because("Snyk ønsker versjon 2.4.5 eller høyere")
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }

    withType<ShadowJar> {
        archiveClassifier.set("")
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }
}

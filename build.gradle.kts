import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "3.1.2"
val coroutinesVersion = "1.6.4"
val sosialhjelpCommonVersion = "1.20230209.0920-45d9782"
val filformatVersion = "1.2023.06.21-14.54-583dfcc41d77"
val tokenValidationVersion = "3.1.0"
val jacksonVersion = "2.14.2"
val springdocversion = "2.2.0"
val jsonSmartVersion = "2.5.0"
val mockOauth2ServerVersion = "0.5.7"
val junitVersion = "4.13.2"
val log4jVersion = "2.19.0"
val snakeyamlVersion = "2.0"
val svarUtVersion = "1.2.0"
val bouncyCastle = "1.74"
val netty = "4.1.94.Final"

val jakartaActivationApiVersion = "2.1.0"
val jakartaAnnotationApiVersion = "2.1.1"
val jakartaXmlBindApiVersion = "4.0.0"
val jakartaValidationApiVersion = "3.0.2"

plugins {
  kotlin("jvm") version "1.9.0"
  kotlin("plugin.spring") version "1.9.0"
  id("org.springframework.boot") version "3.1.2"
  id("com.github.ben-manes.versions") version "0.46.0"
  id("com.diffplug.spotless") version "6.12.0"
}

group = "no.nav.sbl"

version = "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17

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

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocversion")
  implementation("org.springdoc:springdoc-openapi-starter-common:$springdocversion")

  // SvarUt
  implementation("no.ks.fiks.svarut:svarut-rest-klient:$svarUtVersion")

  testImplementation("no.nav.security:token-validation-spring-test:$tokenValidationVersion")
  testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")

  // spesifikke versjoner oppgradert etter ønske fra snyk
  constraints {
    implementation("net.minidev:json-smart:$jsonSmartVersion") {
      because("Dependabot ønsker versjon 2.4.5 eller høyere")
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
      because("https://security.snyk.io/vuln/SNYK-JAVA-ORGYAML-3152153")
    }
    implementation("org.bouncycastle:bcprov-jdk18on:$bouncyCastle") {
      because("https://github.com/advisories/GHSA-hr8g-6v94-x4m9")
    }
    implementation("io.netty:netty-handler:$netty") {
      because("https://github.com/advisories/GHSA-6mjq-h674-j845")
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
  rejectVersionIf { candidate.version.isNonStable() && !currentVersion.isNonStable() }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
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
    indentWithSpaces()
    endWithNewline()
  }
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

val installPreCommitHook =
    tasks.register("installPreCommitHook", Copy::class) {
      from(File(rootProject.rootDir, "scripts/pre-commit"))
      into(File(rootProject.rootDir, ".git/hooks"))
      fileMode = 0b111101101
      dirMode = 0b1010001010
    }

tasks.build.get().dependsOn(installPreCommitHook)

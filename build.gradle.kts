import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "3.1.5"
val coroutinesVersion = "1.7.3"
val sosialhjelpCommonVersion = "1.20231004.1011-d57fe70"
val filformatVersion = "1.2023.09.05-13.49-b12f0a7b2b4a"
val tokenValidationVersion = "3.1.7"
val jacksonVersion = "2.15.3"
val springdocversion = "2.2.0"
val jsonSmartVersion = "2.5.0"
val mockOauth2ServerVersion = "2.0.1"
val junitVersion = "4.13.2"
val log4jVersion = "2.19.0"
val snakeyamlVersion = "2.0"
val svarUtVersion = "2.0.0"
val bouncyCastle = "1.74"
val netty = "4.1.94.Final"

plugins {
  kotlin("jvm") version "1.9.10"
  kotlin("plugin.spring") version "1.9.10"
  id("org.springframework.boot") version "3.1.5"
  id("com.github.ben-manes.versions") version "0.50.0"
  id("com.diffplug.spotless") version "6.22.0"
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

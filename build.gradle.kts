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
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.plugin.spring)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.versions)
  alias(libs.plugins.spotless)
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
  implementation(kotlin("reflect"))

  implementation(libs.spring.boot.starter)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.jackson.module.kotlin)
  implementation(libs.sosialhjelp.common.api)
  implementation(libs.soknadsosialhjelp.filformat)
  implementation(libs.token.validation.spring)
  implementation(libs.token.validation.spring.test)
  implementation(libs.mock.oauth2.server)
  implementation(libs.springdoc.openapi.starter.webmvc.ui)
  implementation(libs.springdoc.openapi.starter.common)
  implementation(libs.svarut.rest.klient)
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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    jvmTarget = "21"
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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val sosialhjelpCommonVersion = "1.2d56711"
val filformatVersion = "1.2020.01.09-15.55-f18d10d7d76a"
val tjenestespesifikasjon = "1.2019.09.25-00.21-49b69f0625e0"
val oidcsupportversion = "0.2.18"
val oauth2oidcsdkversion = "7.3"
val tokenValidationVersion = "1.1.5"
val jacksonVersion = "2.10.3"
val swaggerversion = "2.9.2"
val guavaVersion = "29.0-jre"
val jerseyMediaJaxb = "2.31"

plugins {
    application
	id("org.springframework.boot") version "2.3.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("com.github.johnrengelman.shadow") version "5.2.0"
	kotlin("jvm") version "1.4.10"
	kotlin("plugin.spring") version "1.4.10"
}

val mainClass = "no.nav.sbl.sosialhjelp_mock_alt.MockAltApplicationKt"

application {
    applicationName = "sosialhjelp-mock-alt-api"
    mainClassName = mainClass
}

group = "no.nav.sbl"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

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
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("no.nav.sosialhjelp:sosialhjelp-common-api:$sosialhjelpCommonVersion")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
	implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:$filformatVersion")
	implementation("no.nav.tjenestespesifikasjoner:kodeverk-tjenestespesifikasjon:$tjenestespesifikasjon")
	implementation("no.nav.security:oidc-support:$oidcsupportversion")
	implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
	implementation("no.nav.security:token-validation-test-support:$tokenValidationVersion")
	implementation("com.nimbusds:oauth2-oidc-sdk:$oauth2oidcsdkversion")

	implementation("io.springfox:springfox-swagger2:${swaggerversion}")
	implementation("io.springfox:springfox-swagger-ui:${swaggerversion}")

//	anbefalte versjoner av snyk:
	implementation("com.google.guava:guava:$guavaVersion")
	implementation("org.glassfish.jersey.media:jersey-media-jaxb:$jerseyMediaJaxb")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
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
		classifier = ""
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

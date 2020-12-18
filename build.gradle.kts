import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "2.3.7.RELEASE"
val sosialhjelpCommonVersion = "1.4ef74b4"
val filformatVersion = "1.2020.11.05-09.32-14af05dea965"
val oauth2oidcsdkversion = "7.3"
val tokenValidationVersion = "1.3.2"
val jacksonVersion = "2.12.0"
val swaggerversion = "2.9.2"
val guavaVersion = "30.1-jre"
val jerseyMediaJaxb = "2.31"

plugins {
    application
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	id("com.github.johnrengelman.shadow") version "5.2.0"
	kotlin("jvm") version "1.4.21"
	kotlin("plugin.spring") version "1.4.21"
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
	implementation(kotlin("stdlib-jdk8"))
	implementation(kotlin("reflect"))

	implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

	implementation("no.nav.sosialhjelp:sosialhjelp-common-api:$sosialhjelpCommonVersion")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
	implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:$filformatVersion")
	implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
	implementation("no.nav.security:token-validation-test-support:$tokenValidationVersion")
	implementation("com.nimbusds:oauth2-oidc-sdk:$oauth2oidcsdkversion")

	implementation("io.springfox:springfox-swagger2:${swaggerversion}")
	implementation("io.springfox:springfox-swagger-ui:${swaggerversion}")

	testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")

//    spesifikke versjoner oppgradert etter ønske fra snyk
	constraints {
		implementation("com.google.guava:guava:$guavaVersion") {
			because("Forcer oppgradering av transitiv avhengighet")
		}

		implementation("org.glassfish.jersey.media:jersey-media-jaxb:$jerseyMediaJaxb") {
			because("Transitiv avhengighet dratt inn av token-validation-test-support@1.3.2 har sårbarhet. Constraintsen kan fjernes når token-validation-test-support bruker jersey-media-jaxb@2.31 eller nyere")
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

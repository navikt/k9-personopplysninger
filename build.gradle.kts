import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val junitVersion = "6.0.2"
val k9rapidVersion = "1.20260128091601-a9f852d"
val ktorVersion = "3.4.0"
val dusseldorfKtorVersion = "7.0.7"
val jsonassertVersion = "1.5.3"
val orgJsonVersion = "20251224"
val mockkVersion = "1.14.9"
val assertjVersion = "3.27.7"
val okhttpVersion = "5.3.2"

val appMainClass = "no.nav.omsorgspenger.AppKt"

plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.1"
    id("org.sonarqube") version "7.2.2.6593"
    jacoco
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation("no.nav.k9.rapid:river:$k9rapidVersion")
    implementation("no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    implementation("org.json:json:$orgJsonVersion")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    implementation("com.squareup.okhttp3:okhttp-jvm")

    testImplementation ("org.skyscreamer:jsonassert:$jsonassertVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testImplementation("no.nav.k9.rapid:river-test:$k9rapidVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

repositories {
    mavenLocal()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/k9-rapid")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: "k9-personopplysninger"
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    mavenCentral()
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy(jacocoTestReport) // report is always generated after tests run
    }

    withType<ShadowJar> {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to appMainClass
                )
            )
        }
    }

    withType<JacocoReport> {
        dependsOn(test) // tests are required to run before generating the report
        reports {
            xml.required = true
            csv.required = true
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "navikt_k9-personopplysninger")
        property("sonar.organization", "navikt")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", System.getenv("SONAR_TOKEN"))
        property("sonar.sourceEncoding", "UTF-8")
    }
}

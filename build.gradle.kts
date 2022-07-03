import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.sonarqube") version "3.4.0.2513"
}

group = "com.gioia"
version = "1.0"
var gson = "2.9.0"
var http4k = "4.27.1.0"

sonarqube {
    properties {
        property("sonar.projectKey", "lucas-gio_objetives")
        property("sonar.organization", "lucas-gio")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation("com.google.code.gson:gson:$gson")
    implementation("org.http4k:http4k-client-jetty:$http4k")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().all{
    kotlinOptions.jvmTarget = "16"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

compose.desktop {
    application {
        mainClass = "com.gioia.objetives.MainKt"
        nativeDistributions {
            windows {
                //iconFile.set(project.file("icon.ico"))
            }
            linux {
                //iconFile.set(project.file("icon.png"))
            }
         /*   targetFormats(
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.Exe,
                TargetFormat.Msi)*/
            packageName = "Objetives"
            description = ""
            //packageVersion = "1.0.0"
        }
    }
}
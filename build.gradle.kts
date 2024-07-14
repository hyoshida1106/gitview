plugins {
    kotlin("jvm") version "1.9.21"
    application
    id("org.jetbrains.kotlin.plugin.scripting") version "1.9.21"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("app.cash.sqldelight") version "2.0.1"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "org.progs"
version = "0.1.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    implementation("org.slf4j:slf4j-simple:2.0.11")
    implementation(kotlin("reflect"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.10")

    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:win")
    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:linux")
    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:mac")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.progs.gitview.MainKt")
}

kotlin {
    jvmToolchain(21)
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("org.progs.gitview")
            srcDirs.setFrom("src/main/sqldelight")
            schemaOutputDirectory.set(File("src/main/sqldelight/schema"))
            verifyMigrations.set(true)
        }
    }
}
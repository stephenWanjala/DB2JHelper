plugins {
    kotlin("jvm") version "2.1.10"
}

group = "io.github.stephenWanjala"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api(project(":dbjhelper"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
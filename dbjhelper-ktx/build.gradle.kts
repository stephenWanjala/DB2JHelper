plugins {
    `java-library`
    kotlin("jvm") version "2.2.0"
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.stephenWanjala"
version = "2.0.0"
mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "dbjhelper-ktx", version.toString())

    pom {
        name = "dbjhelper-ktx"
        description =
            "Simplified DB2 Database Operations for Java A lightweight, modern Java library for effortless DB2 database interactions  Kotlin Extensions"
        inceptionYear = "2025"
        url = "https://github.com/stephenWanjala/DB2JHelper"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "dbjhelper-ktx"
                name = "Wanjala Stephen"
                url = "github.com/stephenWanjala/"
            }
        }
        scm {
            url = "  https://github.com/stephenWanjala/DB2JHelper"
            connection = "scm:git:git:/github.com/stephenWanjala/DB2JHelper.git"
            developerConnection = "scm:git:ssh://git@github.com//stephenWanjala/DB2JHelper.git"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.reflect)

    api(libs.kotlinx.coroutines.core)
    api(project(":dbjhelper"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
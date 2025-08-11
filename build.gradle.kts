plugins {
    id("java")
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "io.github.stephenWanjala"
version = "2.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
    }
}


plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.stephenWanjala"
version = "2.0.0"

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "dbjhelper", version.toString())

    pom {
        name = "DB2JHelper"
        description =
            "Simplified DB2 Database Operations for Java A lightweight, modern Java library for effortless DB2 database interactions "
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
                id = "dbjhelper"
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
dependencies {
    api(libs.hikariCp)
    api(libs.db2jcc4)

    implementation(libs.slf4j.api)
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("com.h2database:h2:2.2.224")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

plugins {
    `java-library`
    id("maven-publish")
    id("signing")
}

group = "io.github.stephenWanjala"
version = "1.0.0"

dependencies {
    api("com.zaxxer:HikariCP:5.0.1")
    api("com.ibm.db2.jcc:db2jcc:db2jcc4")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("com.h2database:h2:2.2.224")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "DB2JHelper"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("DB2JHelper")
                description.set("A developer-friendly Java library for DB2 database operations")
                url.set("https://github.com/stephenWanjala/DB2JHelper")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("stephenWanjala")
                        name.set("Stephen Wanjala")
                        email.set("stephenwanjala145@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/stephenWanjala/DB2JHelper.git")
                    developerConnection.set(
                        "scm:git:ssh://github.com:stephenWanjala/DB2JHelper.git"
                    )
                    url.set("https://github.com/stephenWanjala/DB2JHelper")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("ORG_GRADLE_PROJECT_signingKey"),
        System.getenv("ORG_GRADLE_PROJECT_signingPassword")
    )
    sign(publishing.publications["maven"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

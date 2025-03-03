plugins {
    id("java")
}

group = "io.github.stephenWanjala"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":dbjhelper"))
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.ibm.db2.jcc:db2jcc:db2jcc4")
}

tasks.test {
    useJUnitPlatform()
}
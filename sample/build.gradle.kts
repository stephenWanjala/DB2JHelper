plugins {
    id("java")
    application
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
    
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("org.slf4j:slf4j-api:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("io.github.stephenwanjala.sample.SampleApplication")
}
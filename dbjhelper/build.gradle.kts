
java{
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
plugins {
//    `java-library`
    id("java")
    id("maven-publish")

}

group = "io.github.stephenWanjala"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.zaxxer:HikariCP:3.4.1")
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    id("java")
}

group = "dev.gurwi"
version = "0.1.3"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    implementation("org.yaml:snakeyaml:2.4")
    implementation("org.jetbrains:annotations:24.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
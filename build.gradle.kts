plugins {
    id("java")
    id("maven-publish")
    signing
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "Confetti"
                description = "A lightweight YAML configuration library for Java, built on top of SnakeYAML."
                url = "https://github.com/Gurwi30/confetti"

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/Gurwi30/confetti.git"
                    developerConnection = "scm:git:ssh://git@github.com/Gurwi30/confetti.git"
                    url = "https://github.com/Gurwi30/confetti"
                }
            }
        }
    }

    repositories {
        maven {
            name = "Central"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")

            credentials {
                username = providers.environmentVariable("MAVEN_CENTRAL_USERNAME").orNull
                password = providers.environmentVariable("MAVEN_CENTRAL_PASSWORD").orNull
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
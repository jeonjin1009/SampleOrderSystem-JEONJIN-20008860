plugins {
    java
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

group = "com.ssemi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")
}

application {
    mainClass.set("com.ssemi.sampleorder.App")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
plugins {
    id("io.micronaut.application") version "4.1.1"
}

version = "0.0.1-SNAPSHOT"
group = "io.jaconi"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")

    testImplementation("io.micronaut.test:micronaut-test-rest-assured")
}

application {
    mainClass.set("io.jaconi.Application")
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

graalvmNative.toolchainDetection.set(false)
micronaut {
    version("4.1.1")
    runtime("netty")
    testRuntime("junit5")
}

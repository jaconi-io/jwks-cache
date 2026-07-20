plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "1.1.5"
}

group = "io.jaconi"
version = "0.0.1-SNAPSHOT"

dependencies {
	annotationProcessor("org.projectlombok:lombok")

	compileOnly("org.projectlombok:lombok")

	implementation("com.nimbusds:nimbus-jose-jwt:10.9.1")
	implementation("io.kubernetes:client-java:27.0.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	testImplementation("org.mock-server:mockserver-junit-jupiter:7.4.0")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-k3s")

	// Required by MockServer.
	testRuntimeOnly("io.prometheus:prometheus-metrics-tracer-initializer")
}

java {
	sourceCompatibility = JavaVersion.VERSION_25
}

repositories {
	mavenCentral()
}

tasks.bootBuildImage {
	createdDate = "now"
	imageName = "ghcr.io/jaconi-io/${project.name}"
}

tasks.jacocoTestReport {
	reports {
		xml.required = true
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

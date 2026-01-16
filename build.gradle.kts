plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.11.3"
}

group = "io.jaconi"
version = "0.0.1-SNAPSHOT"

dependencies {
	annotationProcessor("org.projectlombok:lombok")

	compileOnly("org.projectlombok:lombok")

	implementation("com.nimbusds:nimbus-jose-jwt:10.7")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	testImplementation("org.mock-server:mockserver-junit-jupiter:5.15.0")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
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

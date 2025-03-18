plugins {
	java
	jacoco
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "io.jaconi"
version = "0.0.1-SNAPSHOT"

dependencies {
	annotationProcessor("org.projectlombok:lombok")

	compileOnly("org.projectlombok:lombok")

	implementation("com.nimbusds:nimbus-jose-jwt:10.0.2")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	testImplementation("org.mock-server:mockserver-junit-jupiter:5.15.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

tasks.bootBuildImage {
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

graalvmNative {
	binaries {
		names.forEach { binaryName ->
			named(binaryName){
				if (!setOf("x86_64", "amd64").contains(System.getProperty("os.arch"))) {
					buildArgs.add("-Ob")
				}
			}
		}
	}
}
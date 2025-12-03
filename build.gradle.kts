plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"
description = "DevMarketBackEnd"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.1.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("io.projectreactor:reactor-test")

    //DataBase
    runtimeOnly("com.mysql:mysql-connector-j")

    //Swagger
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    //Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    //Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //Spring Boot Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-config")

    //oauth2
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")

    //DotEnv
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    //JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    //AWS
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

    //kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")


}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// Java 소스 디렉토리 추가 (Kotlin 디렉토리 내의 Java 파일 컴파일)
sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}



plugins {
    java
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.1.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

springBoot {
    mainClass.set("cherry.testtool.demo.DemoApplication")
}

base {
    archivesName.set("cherry-testtool-demo")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories { mavenCentral() }

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
    }
}

dependencies {
    // libをマルチプロジェクトのサブプロジェクトとして参照する。
    implementation(project(":lib"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Spring Boot 4.xでは@AutoConfigureMockMvc等がspring-boot-starter-webmvc-testへ分離されているため追加。
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

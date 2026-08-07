import org.gradle.internal.os.OperatingSystem

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
    mainClass.set("cherry.testtool.webconsole.WebconsoleApplication")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories { mavenCentral() }

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.2")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Spring Boot 4.xでは@SpringBootTest等のWebスライステストがspring-boot-starter-webmvc-testへ分離されているため追加。
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// フロントエンド(frontend/、React SPA)をnpmでビルドし、静的リソースとしてjarへ組み込む。
val npmCommand = if (OperatingSystem.current().isWindows) "npm.cmd" else "npm"

val npmInstall by tasks.registering(Exec::class) {
    workingDir = file("frontend")
    inputs.file("frontend/package.json")
    inputs.file("frontend/package-lock.json")
    outputs.dir("frontend/node_modules")
    commandLine(npmCommand, "install")
}

val npmBuild by tasks.registering(Exec::class) {
    dependsOn(npmInstall)
    workingDir = file("frontend")
    inputs.dir("frontend/src")
    inputs.dir("frontend/public")
    inputs.file("frontend/index.html")
    inputs.file("frontend/vite.config.ts")
    outputs.dir("frontend/dist")
    commandLine(npmCommand, "run", "build")
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(npmBuild)
    from("frontend/dist") {
        into("static")
    }
}

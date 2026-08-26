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

base {
    archivesName.set("cherry-testtool-webconsole")
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
    implementation("org.jspecify:jspecify")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Spring Boot 4.xでは@SpringBootTest等のWebスライステストがspring-boot-starter-webmvc-testへ分離されているため追加。
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// フロントエンド(frontend/、React SPA)をnpmでビルドし、静的リソースとしてjarへ組み込む。
val npmCommand = if (OperatingSystem.current().isWindows) "npm.cmd" else "npm"

// frontendが file: 参照するsubmodule(vendor/make-you-chic-ui)は、git submoduleのcheckoutだけでは
// distが無く(.gitignore対象、ビルド成果物のため)利用できない。npm workspace(vendor/make-you-chic-ui直下)の
// installとbuildを事前に済ませておく(クリーンな環境(fresh clone + submodule init)から常に再現可能にするため)。
val vendorDir = file("frontend/vendor/make-you-chic-ui")

val vendorInstall = tasks.register<Exec>("vendorInstall") {
    workingDir = vendorDir
    inputs.file("$vendorDir/package.json")
    inputs.file("$vendorDir/package-lock.json")
    outputs.dir("$vendorDir/node_modules")
    commandLine(npmCommand, "install")
}

val vendorBuild = tasks.register<Exec>("vendorBuild") {
    dependsOn(vendorInstall)
    workingDir = vendorDir
    inputs.dir("$vendorDir/packages/make-you-chic-ui/src")
    outputs.dir("$vendorDir/packages/make-you-chic-ui/dist")
    commandLine(npmCommand, "run", "build")
}

val npmInstall = tasks.register<Exec>("npmInstall") {
    dependsOn(vendorBuild)
    workingDir = file("frontend")
    inputs.file("frontend/package.json")
    inputs.file("frontend/package-lock.json")
    outputs.dir("frontend/node_modules")
    commandLine(npmCommand, "install")
}

val npmBuild = tasks.register<Exec>("npmBuild") {
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

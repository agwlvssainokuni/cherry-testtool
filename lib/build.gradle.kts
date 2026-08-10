plugins {
    `java-library`
    id("io.spring.dependency-management") version "1.1.7"
}

base {
    archivesName.set("cherry-testtool-core")
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

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "cherry.testtool.core")
    }
}

val javaagent = configurations.create("javaagent")

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-javaagent:${javaagent.asPath}")
    jvmArgs("-Dpolyglot.engine.WarnInterpreterOnly=false")
}

repositories { mavenCentral() }

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
    }
}

dependencies {

    // Nullability注釈(org.jspecify.annotations.Nullable等)。公開APIのシグネチャに現れるためapiとする。
    api("org.jspecify:jspecify")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml")
    runtimeOnly("org.graalvm.js:js")
    runtimeOnly("org.graalvm.js:js-scriptengine")

    // テストライブラリ。
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-json")
    // Spring Boot 4.xでは@WebMvcTest等のWebスライステストがspring-boot-starter-webmvc-testへ分離されているため追加。
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    // @WebMvcTest/MockMvcがSpring MVCの型をコンパイル時に要求するためtestImplementationとする。
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testRuntimeOnly("org.springframework.boot:spring-boot-starter-aspectj")
    testRuntimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    testRuntimeOnly("io.micrometer:micrometer-tracing-bridge-brave")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    "javaagent"("org.mockito:mockito-core") { isTransitive = false }
}

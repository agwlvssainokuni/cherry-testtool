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
    mainClass.set("cherry.testtool.cli.CliApplication")
}

base {
    archivesName.set("cherry-testtool-cli")
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
    // Nullability注釈。バージョンは明示指定する(io.spring.dependency-managementの管理外のため)。
    implementation("org.jspecify:jspecify:1.0.0")

    implementation("org.springframework.boot:spring-boot-starter")
    // RestClient/HttpServiceProxyFactory/@HttpExchangeはspring-webモジュールのみで足りる。
    // 組込みTomcat等は不要のためspring-boot-starter-webは使わない(spring.main.web-application-type=noneと併せて起動を軽量化)。
    implementation("org.springframework:spring-web")
    // TesttoolApiClient#getStub(List<String>戻り値)のJSONデシリアライズに必要。
    // RestClientはクラスパス上にJackson等が存在する場合のみJSON用HttpMessageConverterを自動登録するため、
    // spring-boot-starter-webに依存しないこのモジュールでは明示的に追加する必要がある。
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("info.picocli:picocli-spring-boot-starter:4.7.7")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

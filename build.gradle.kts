import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("io.spring.dependency-management") version "1.1.7" apply false
}

// io.spring.dependency-managementはプロジェクト単位のresolution strategyとして実装されており、
// 複合ビルド(includeBuild)はもちろん、同一マルチプロジェクト内のproject依存(project(":lib")等)を
// 跨いでも管理対象バージョンは伝播しない。そのため、複数モジュールから参照されうる依存のバージョンは、
// 各サブプロジェクトが同一の値で管理するようここへ集約する(各サブプロジェクト自身のspring-boot-dependencies等の
// BOMインポートは、モジュール毎に必要なものが異なるため各build.gradle.ktsに残す)。
subprojects {
    plugins.withId("io.spring.dependency-management") {
        configure<DependencyManagementExtension> {
            dependencies {
                dependency("org.jspecify:jspecify:1.0.0")
                dependency("org.graalvm.js:js:25.2.4")
                dependency("org.graalvm.js:js-scriptengine:25.2.4")
                dependency("info.picocli:picocli-spring-boot-starter:4.7.7")
            }
        }
    }
}

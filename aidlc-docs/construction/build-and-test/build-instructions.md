# Build Instructions

## Prerequisites
- **Build Tool**: Gradle(リポジトリ直下1つのWrapper、`./gradlew`)、Java 25 toolchain(Gradleが自動解決)
- **Dependencies**: Mavenリポジトリ(`mavenCentral()`)への到達性が必要(初回ビルド時に依存jarをダウンロードする)
- **Frontend**: `client:webconsole`のみnpm(Node.js)が必要。ビルド時に`npmInstall`/`npmBuild`タスクが自動実行される(volta/nvm等でnpmをインストールしている場合、Gradle daemonがPATHを再取得できるよう`./gradlew --stop`後に初回ビルドすることを推奨)
- **System Requirements**: 通常のノートPC/CI環境で問題なし。特別なメモリ・ディスク要件は無い

## モジュール構成

リポジトリ全体は単一のGradleマルチプロジェクトビルド(`rootProject.name = "cherry-testtool"`、リポジトリ直下に1つの`settings.gradle.kts`/`gradlew`)。4サブプロジェクトから成る。

```
cherry-testtool(root)
├─ :lib                    (lib/、成果物名 cherry-testtool-core)
├─ :demo                   (demo/、成果物名 cherry-testtool-demo。implementation(project(":lib")))
├─ :client:webconsole      (client/webconsole/、成果物名 cherry-testtool-webconsole。lib非依存)
└─ :client:cli             (client/cli/、成果物名 cherry-testtool-cli。lib非依存)
```

`demo`は`:lib`をGradleプロジェクト依存(`project(":lib")`)として直接参照する。`webconsole`・`cli`はビルド時に`lib`へ依存しない(実行時にHTTP経由で`demo`等へアクセスするのみ)。

リポジトリ直下には最小限の`build.gradle.kts`も存在する。`io.spring.dependency-management`のバージョン管理はプロジェクト単位のresolution strategyであり、複合ビルドはもちろん真のマルチプロジェクトのproject依存(`project(":lib")`)を跨いでも伝播しないため、複数モジュールから参照されうる依存(`jspecify`、`commons-collections4`、GraalVM JS関連、`picocli-spring-boot-starter`)のバージョンは、`subprojects { }`ブロックで一元管理している(各モジュール固有のBOMインポート(`spring-boot-dependencies`等)は各モジュール自身の`build.gradle.kts`に残置)。

**経緯**: 当初は各モジュールを完全に独立したGradleビルド(`demo`のみ`includeBuild`で`lib`をソース参照する複合ビルド)としていたが、IntelliJ IDEAで`lib`が「単独リンクされたプロジェクト」と「`demo`のincludeBuild先」の両方として扱われることでビルドスクリプトの解析が競合し、`lib/build.gradle.kts`・`settings.gradle.kts`にのみ偽陽性のエラーが表示される問題が発生した。単一`settings.gradle.kts`配下のマルチプロジェクトへ統合することで、この種のIDE側の構造的競合を解消した。

## Build Steps

### 1. 全モジュールをビルドする

リポジトリ直下から一括でビルドする。

```bash
./gradlew build
```

個別のサブプロジェクトのみビルドする場合はGradleパスを指定する。

```bash
./gradlew :lib:build
./gradlew :demo:build
./gradlew :client:webconsole:build
./gradlew :client:cli:build
```

### 2. 実行可能jarの生成

`demo`・`webconsole`・`cli`はSpring Bootアプリケーションのため、`bootJar`タスクで実行可能jarを生成できる(`lib`はライブラリのため通常の`jar`タスク)。

```bash
./gradlew :lib:jar                     # lib/build/libs/cherry-testtool-core.jar
./gradlew :demo:bootJar                # demo/build/libs/cherry-testtool-demo.jar
./gradlew :client:webconsole:bootJar   # client/webconsole/build/libs/cherry-testtool-webconsole.jar
./gradlew :client:cli:bootJar          # client/cli/build/libs/cherry-testtool-cli.jar
```

### 3. ビルド成功の確認

- **Expected Output**: `BUILD SUCCESSFUL`(全サブプロジェクトのタスクが一括実行される)
- **Build Artifacts**: `lib/build/libs/cherry-testtool-core.jar`(ライブラリjar)、`demo/build/libs/cherry-testtool-demo.jar`、`client/webconsole/build/libs/cherry-testtool-webconsole.jar`(SPA静的リソース同梱)、`client/cli/build/libs/cherry-testtool-cli.jar`
- **Common Warnings**: `client:webconsole`初回ビルド時、`npm audit`由来の脆弱性件数レポート(依存パッケージの既知の警告、ビルド失敗の原因ではない)

## Troubleshooting

### `client:webconsole`の`npmInstall`タスクが `A problem occurred starting process 'command 'npm''` で失敗する
- **Cause**: Gradle daemonが起動時点の`PATH`環境変数をキャッシュしており、volta/nvm等でインストールしたnpmのパスを認識していない
- **Solution**: `./gradlew --stop`でdaemonを停止してから再度ビルドする(新しいdaemonが現在のシェルの`PATH`を引き継ぐ)

### IntelliJ IDEAで特定のサブプロジェクトの`build.gradle.kts`にのみ偽陽性のエラーが表示される
- **Cause**: マルチプロジェクト化前に発生していた既知の問題(上記「モジュール構成」の経緯参照)。マルチプロジェクト化後は基本的に発生しない
- **Solution**: File → Invalidate Caches / Restart → Invalidate and Restart。それでも解消しない場合はGradleツールウィンドウで「Reload All Gradle Projects」を実行する

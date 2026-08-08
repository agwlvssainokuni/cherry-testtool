# Build Instructions

## Prerequisites
- **Build Tool**: Gradle(各モジュール同梱のWrapper、`./gradlew`)、Java 25 toolchain(Gradleが自動解決)
- **Dependencies**: Mavenリポジトリ(`mavenCentral()`)への到達性が必要(初回ビルド時に依存jarをダウンロードする)
- **Frontend**: `client/webconsole`のみnpm(Node.js)が必要。ビルド時に`npmInstall`/`npmBuild`タスクが自動実行される(volta/nvm等でnpmをインストールしている場合、Gradle daemonがPATHを再取得できるよう`./gradlew --stop`後に初回ビルドすることを推奨)
- **System Requirements**: 通常のノートPC/CI環境で問題なし。特別なメモリ・ディスク要件は無い

## モジュール構成とビルド順序

4モジュールは互いに`includeBuild`等のGradle複合ビルド関係を持たず(`demo`が`lib`をソース参照する複合ビルドのみ例外)、それぞれ独立してビルド可能。ビルド時の依存関係は以下の通り。

```
lib (cherry-testtool-core)
 └─ demo (cherry-testtool-demo)   ※includeBuild("../lib")でlibをソース参照

client/webconsole (cherry-testtool-webconsole)   ※ビルド時はlib/demoに非依存
client/cli (cherry-testtool-cli)                 ※ビルド時はlib/demoに非依存
```

`webconsole`・`cli`は実行時にHTTP経由で`demo`(またはlibを組み込んだ任意のアプリ)へアクセスするが、ビルド時の依存関係は無い。4モジュールは並行してビルド可能。

## Build Steps

### 1. 全モジュールをビルドする

```bash
cd lib && ./gradlew build
cd ../demo && ./gradlew build
cd ../client/webconsole && ./gradlew build
cd ../cli && ./gradlew build
```

(`demo`は`lib`を`includeBuild`経由でソース参照するため、`lib`を先にビルドしておく必要は無い。`demo`のビルド時に自動的に`lib`もコンパイルされる。)

### 2. 実行可能jarの生成

`demo`・`webconsole`・`cli`はSpring Bootアプリケーションのため、`./gradlew bootJar`で実行可能jarを生成できる(`lib`はライブラリのため`bootJar`は無い)。

```bash
cd demo && ./gradlew bootJar          # demo/build/libs/cherry-testtool-demo.jar
cd client/webconsole && ./gradlew bootJar  # client/webconsole/build/libs/cherry-testtool-webconsole.jar
cd client/cli && ./gradlew bootJar    # client/cli/build/libs/cherry-testtool-cli.jar
```

### 3. ビルド成功の確認

- **Expected Output**: 各モジュールで`BUILD SUCCESSFUL`
- **Build Artifacts**: `lib/build/libs/cherry-testtool-*.jar`(ライブラリjar)、`demo/build/libs/cherry-testtool-demo.jar`、`client/webconsole/build/libs/cherry-testtool-webconsole.jar`(SPA静的リソース同梱)、`client/cli/build/libs/cherry-testtool-cli.jar`
- **Common Warnings**: `client/webconsole`初回ビルド時、`npm audit`由来の脆弱性件数レポート(依存パッケージの既知の警告、ビルド失敗の原因ではない)

## Troubleshooting

### `client/webconsole`の`npmInstall`タスクが `A problem occurred starting process 'command 'npm''` で失敗する
- **Cause**: Gradle daemonが起動時点の`PATH`環境変数をキャッシュしており、volta/nvm等でインストールしたnpmのパスを認識していない
- **Solution**: `./gradlew --stop`でdaemonを停止してから再度ビルドする(新しいdaemonが現在のシェルの`PATH`を引き継ぐ)

### `demo`のビルドが`Could not find ...`系の依存解決エラーで失敗する
- **Cause**: `lib`の依存が`io.spring.dependency-management`のBOM管理下にあり、複合ビルド(`includeBuild`)を跨いでバージョン解決できていない
- **Solution**: `lib/build.gradle.kts`の該当依存にバージョンが明記されているか確認する(現状は全て明記済みのため通常発生しない)

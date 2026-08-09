# cherry-testtool-core

`lib`(成果物名`cherry-testtool-core`)は、Spring Bootアプリへ以下2つの機能を組み込むためのライブラリである。

- **InvokerService** — リフレクションによるSpring Beanメソッドの動的呼出し
- **StubResolver/StubRepository** — Spring AOPによるメソッド戻り値のスタブ差し替え(GraalVM JavaScriptでスクリプト記述)

本書は、本リポジトリの外にある別プロジェクト(Gradle Wrapper管理)へ`lib`を組み込む手順を示す。

## この手順書のスコープ

`lib`は`/testtool/**`で任意メソッドの呼出し・スタブ登録ができる強力なAPIを公開するため、Mavenリポジトリ等への公開(publish)は行わない方針とし、**ビルド成果物(jar)を手元でコピーして使う方式**を前提とする。`lib`を更新するたびに本手順の①からやり直し、jarを再取得・再配置すること。

## 前提条件(消費側プロジェクト)

- Gradle Wrapperで管理されたプロジェクトであること
- Java 25以上
- Spring Boot 4.1.0系のBOM(`org.springframework.boot:spring-boot-dependencies:4.1.0`)をimportしていること。バージョンがずれると、`lib`が内部で使用しているSpring APIとの不整合が実行時エラーとして現れる恐れがある

## 手順

### 1. jarをビルドする

本リポジトリで以下を実行する。

```bash
./gradlew :lib:jar
```

`lib/build/libs/cherry-testtool-core.jar`が生成される(`version`を設定していないため、ファイル名にバージョン番号は付かない)。

### 2. 消費側プロジェクトへ配置する

消費側リポジトリ直下に`libs/`ディレクトリを作り、上記jarをコピーする。

```
your-app/
├── libs/
│   └── cherry-testtool-core.jar
├── build.gradle.kts
└── ...
```

### 3. build.gradle.ktsへ依存関係を追加する

`files()`依存にはPOMが無く推移的依存が解決されないため、`lib`が必要とする依存を消費側で明示的に追加する必要がある。以下、Spring Boot BOMで管理されるためバージョン省略可能なものと、BOM対象外のためバージョン明記が必要なもの(`lib`自身が使用しているバージョンに合わせる)を分けて示す。

```kotlin
dependencies {
    // lib本体
    implementation(files("libs/cherry-testtool-core.jar"))

    // Spring Boot 4.1.0のBOMで管理されるためバージョン省略可
    // (消費側が同BOMをimportしていることが前提)
    implementation("org.apache.commons:commons-lang3")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    // libではcompileOnly扱い(webを前提としない設計)のため、
    // REST API(/testtool/**)を使う場合は消費側での追加が必須
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml")

    // Spring Boot BOM対象外のためバージョン明記が必要
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("org.apache.commons:commons-collections4:4.5.0")
    runtimeOnly("org.graalvm.js:js:25.1.3")
    runtimeOnly("org.graalvm.js:js-scriptengine:25.1.3")
}
```

### 4. AOPスタブを組み込む(スタブ機能を使う場合)

メソッドの戻り値をスタブ差し替えたい場合、対象クラスへのAOP介入を1クラス自分で用意する必要がある。`lib`は`StubResolver`という部品を提供するのみで、どのクラスに介入するかはアプリごとに明示配線が必要なため。

本リポジトリの`demo/src/main/java/cherry/testtool/demo/aspect/StubAspect.java`を参考に、以下2点だけ変更して自分のアプリへ配置する。

1. `@Around`アノテーションのpointcut式(`execution(* cherry.testtool.demo.SampleService.*(..))`)を、実際にスタブ対象としたい自分のクラスへ書き換える
2. パッケージ宣言を自分のアプリの構成に合わせる

`spring-boot-starter-aspectj`が依存関係にあれば、Spring Bootの自動構成(`spring.aop.auto=true`が既定)によりAspectJ自動プロキシが有効になり、追加設定なしでスタブ介入が機能する。

呼出し機能(InvokerService)のみを使う場合、この手順は不要。

### 5. 動作確認する

1. 消費側アプリを起動する
2. 本リポジトリのビルド成果物(`client/cli`または`client/webconsole`)から接続し、対象クラス・メソッドに対して`invoke`/`stubconfig register`を試す
3. スタブ登録前後でメソッドの戻り値が変わることを確認する

```bash
# 本リポジトリ側で事前にビルドしておく
./gradlew :client:cli:bootJar

# 消費側アプリ(例: http://localhost:8080)に対して実行
java -jar client/cli/build/libs/cherry-testtool-cli.jar invoke {DIR}...
java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig register {DIR}...
```

## 設定プロパティ

```properties
# REST API(TesttoolController、/testtool/**)の有効/無効。既定は有効
cherry.testtool.web.enabled=true

# GraalVM JavaScriptエンジンの警告抑制(推奨)
polyglot.engine.WarnInterpreterOnly=false
```

## セキュリティ上の注意

`/testtool/**`はリフレクションによる任意メソッド呼出し・スタブ登録が可能な強力なAPIである。想定外の環境(本番等)へ公開しないよう、プロファイル制御・認証・ネットワーク経路の制限などを消費側アプリで講じること。`lib`自体はアクセス制御を提供しない。

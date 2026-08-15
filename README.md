
# cherry-testtool

Spring Bootアプリケーション向けの動的テストツール。リフレクションによるメソッド呼出しと、AOPによるスタブ差し替えを、JavaScriptと組み合わせて提供する。

## 概要

cherry-testtoolは、Spring Bootアプリケーションのテスト体験を向上させるためのツール群である。任意のSpring Beanメソッドを動的に呼び出したり、メソッドの戻り値をJavaScriptベースのスクリプトでスタブ差し替えしたりできる。Webコンソール(SPA)とCLIの両方から操作可能。

## 特徴

- **動的メソッド呼出し**: リフレクションによりSpring Beanのメソッドを動的に呼び出す
- **AOPベースのスタブ差し替え**: メソッド呼出しをインターセプトし、任意の戻り値に差し替える
- **JavaScript連携**: GraalVM JavaScriptエンジンにより、引数生成・スタブ戻り値の両方をスクリプトで記述できる
- **Webコンソール**: 対話的にメソッド呼出し・スタブ設定を行えるReact SPA(APIプロキシと一体化)
- **CLI**: 自動化・バッチ処理向けのコマンドラインツール
- **デモアプリ**: `lib`を組み込んだ最小構成のリファレンス実装。`webconsole`/`cli`の動作確認先であると同時に、自分のアプリへ`lib`を組み込む際の手引きにもなる

## アーキテクチャ

単一Gradleマルチプロジェクトビルド(`:lib`、`:demo`、`:client:webconsole`、`:client:cli`)。SPA(`client/webconsole/frontend/`)のみnpmで別管理する。

```
cherry-testtool/              # rootProject。全サブプロジェクト共通の settings.gradle.kts / gradlew
├── lib/                      # :lib - コアライブラリ(成果物名 cherry-testtool-core)
│   │                           外部プロジェクトへ組み込む手順は lib/README.md 参照
│   ├── invoker/              # 動的メソッド呼出し(InvokerService)
│   ├── stub/                 # AOPベースのスタブ差し替え(StubRepository/StubResolver/StubConfigLoader)
│   ├── script/                # GraalVM JavaScriptエンジン連携(ScriptProcessor)
│   ├── reflect/               # Spring Bean/メソッド解決のユーティリティ(ReflectionResolver)
│   └── web/                   # REST APIコントローラ(TesttoolController、/testtool/**)
├── demo/                      # :demo - libを組み込んだリファレンスアプリ(port 8080)。詳細は demo/README.md 参照
│   ├── stub-samples/          # スタブ設定スクリプトのサンプル(webconsole/cli共用)
│   └── invoke-samples/        # 引数生成スクリプトのサンプル(webconsole/cli共用)
├── client/
│   ├── webconsole/            # :client:webconsole - SPA + APIプロキシ(Spring Cloud Gateway Server MVC)、port 9090
│   │   └── frontend/          # React SPA本体(npm管理)
│   └── cli/                   # :client:cli - コマンドラインツール(Picocli)
├── e2e/                       # demo+cli+webconsoleをまたぐE2Eテスト(npm管理、Gradleマルチプロジェクト対象外)
└── aidlc-docs/                # AI-DLCによる要件定義・設計・構築ドキュメント一式
```

各モジュールの詳しい使い方は、それぞれの`README.md`を参照。

- [lib/README.md](lib/README.md) — `cherry-testtool-core`を外部プロジェクトへ組み込む手順
- [demo/README.md](demo/README.md) — デモアプリの構成、スタブ組み込み方、サンプルの使い方
- [client/webconsole/README.md](client/webconsole/README.md) — Webコンソールの構成・起動方法
- [client/cli/README.md](client/cli/README.md) — CLIのコマンド一覧・旧シェルスクリプトからの移行ガイド
- [e2e/README.md](e2e/README.md) — E2Eテストの構成・実行方法

## 技術スタック

- **バックエンド**: Java 25、Spring Boot 4.1.0、Spring AOP、GraalVM JavaScript
- **フロントエンド**: React 19、TypeScript、Vite、Material-UI
- **ビルドツール**: Gradle(Java側)、npm(フロントエンド側)
- **テスト**: JUnit 5、Mockito、Hamcrest

## セットアップ

### 前提条件

- Java 25以上
- Node.js 18以上
- npm

### ビルド

リポジトリ直下から一括ビルドする(単一Gradleマルチプロジェクトビルド)。

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

### アプリケーションの起動

#### デモアプリ(動作確認対象)を起動する

```bash
./gradlew :demo:bootRun
```

`http://localhost:8080`で起動する。

#### Webコンソールを起動する

```bash
./gradlew :client:webconsole:bootRun
```

`http://localhost:9090`で起動する(SPA・APIプロキシとも同一ポート)。

#### フロントエンドのみをdev serverで起動する

```bash
cd client/webconsole/frontend
npm install
npm run dev
```

`http://localhost:5173`でアクセスする。

### テスト

#### Javaのテストを実行する

```bash
./gradlew test
```

#### フロントエンドのLintを実行する

```bash
cd client/webconsole/frontend
npm run lint
```

#### E2Eテストを実行する

demo・webconsole・cliをまたぐ一気通貫の自動テスト([e2e/README.md](e2e/README.md)参照)。通常のビルド(`./gradlew build`)には含まれない独立したnpmプロジェクト。

```bash
./gradlew build   # demo/webconsole/cliのjarをビルド

cd e2e
npm install
npm run install:browsers   # 初回のみ
npm run test:e2e
```

## 使い方

### Webコンソール

`http://localhost:5173`(Vite dev server)または`http://localhost:9090`(webconsole。SPA・APIプロキシ双方を提供)でアクセスする。

#### メソッド呼出し(/invoker)

1. `/invoker`を開く
2. 対象クラスのFQCNを指定する
3. Bean名を指定する(省略可)
4. 呼び出すメソッドを選択する
5. 引数を生成するJavaScriptコードを記述する
6. 実行し、結果を確認する

#### スタブ設定(/stubconfig)

1. `/stubconfig`を開く
2. 対象メソッドを選択する
3. 戻り値・例外をJavaScriptで定義する
4. 登録する

### CLI

```bash
./gradlew :client:cli:bootJar

# メソッド呼出し
java -jar client/cli/build/libs/cherry-testtool-cli.jar invoke {DIR}...

# スタブ設定
java -jar client/cli/build/libs/cherry-testtool-cli.jar stubconfig register|show|clear {DIR}...
```

コマンド詳細・オプションは[client/cli/README.md](client/cli/README.md)を参照。

## 主要コンポーネント(lib)

### TesttoolAutoConfiguration

`lib`が提供する全Bean(下記の各コンポーネント)を定義する自動構成クラス。`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`経由でSpring Bootの自動構成として登録される。`TesttoolController`は利用側アプリのコンポーネントスキャン対象外パッケージにあるため、有効/無効の判定(`cherry.testtool.web.enabled`)も含めここで明示的にBean登録している。

### InvokerService

リフレクションによる動的メソッド呼出しを提供する。

- クラス名からSpring Beanを解決する
- JavaScriptで引数を生成する
- メソッドを実行し、結果を整形して返す

### StubRepository / StubResolver

AOPベースのメソッドスタブ差し替えを実装する。

- Spring AOPでメソッド呼出しをインターセプトする
- JavaScript式を評価して戻り値を決定する
- メソッド引数に応じた条件付きスタブに対応する

### StubConfigLoader

指定ディレクトリ配下のスクリプトファイルを一括読込みし、`StubRepository`へスタブ設定として直接登録する。`demo`では`StubAutoLoadRunner`から呼び出し、起動時の自動読込みに利用している(詳細は[demo/README.md](demo/README.md))。

### ScriptProcessor

GraalVM JavaScriptエンジンとの連携を提供する。

- セキュアなコンテキストでJavaScriptコードを実行する
- Spring ApplicationContextへのアクセスを提供する
- 引数生成・スタブ戻り値生成の両方に対応する

### ReflectionResolver

Spring Bean・メソッド解決のユーティリティ。

- クラス名からBeanを検索する
- オーバーロードされたメソッドをパラメータ情報付きで解決する
- メソッドシグネチャの説明文を生成する

## 設定

```properties
# REST API(TesttoolController、/testtool/**)の有効/無効。既定は有効
cherry.testtool.web.enabled=true

# GraalVM JavaScriptエンジンの警告抑制(推奨)
polyglot.engine.WarnInterpreterOnly=false
```

## JavaScript API

### 引数生成スクリプト

配列として引数を生成する。

```javascript
// 単純な引数生成
["arg1", 42, true]

// オブジェクトを組み立てる例
[
  {
    name: "test",
    value: new Date().getTime()
  }
]
```

型がプリミティブ/文字列で表現できない場合(日付型、ネストしたrecord等)は、GraalVM JSの`Java.type(...)`でJavaの型を直接参照して生成する。サンプルは`demo/invoke-samples/`を参照。

### スタブ設定スクリプト

戻り値・振る舞いを定義する。

```javascript
// 固定値を返す
"stubbed result"

// 引数に応じて動的に返す
function(args) {
  return args[0] + " processed";
}

// 条件付きスタブ
args[0] === "test" ? "success" : "failure"
```

## 開発

### プロジェクト構成

- `lib/src/main/java/cherry/testtool/` — コア実装
- `demo/` — `lib`を組み込んだリファレンスアプリ
- `client/webconsole/frontend/src/` — Reactコンポーネント・ページ
- `client/webconsole/src/` — APIプロキシ/SPAホスティングの設定
- `client/cli/src/` — コマンドラインアプリケーション(Picocli)

### 機能追加の流れ

1. `lib`モジュールへコアロジックを実装する
2. Webコントローラへ必要に応じてREST APIを追加する
3. 対応するReactコンポーネントを作成する
4. 必要に応じてCLIを更新する

## ライセンス

Apache License 2.0。詳細は[LICENSE](LICENSE)を参照。

## Copyright

Copyright 2019,2026 agwlvssainokuni

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

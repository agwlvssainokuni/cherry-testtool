# Technology Stack

## Programming Languages
- Java - 25(toolchain指定、`lib`/`client/gateway`共通) - バックエンドライブラリ・ゲートウェイの実装言語
- TypeScript - ^6.0.3 - SPAの実装言語
- Bash - CLIツール(`invoker.sh`、`stubconfig.sh`)の実装言語
- JavaScript(GraalVM JS実行時) - 引数生成/スタブ戻り値生成スクリプトの記述言語

## Frameworks
- Spring Boot - 4.1.0 - `lib`(自動構成ライブラリとして)、`client/gateway`(アプリケーション基盤)
- Spring Cloud Gateway(WebFlux) - Spring Cloudドキュメント上のリリーストレイン`2025.1.2` - `client/gateway`のルーティング基盤
- Spring AOP / AspectJ(spring-boot-starter-aspectj) - `lib`のスタブ介入(`StubInterceptor`)、テストコードの`StubAspect`
- React - ^19.2.7 - SPAのUIフレームワーク
- React Router DOM - ^7.18.1 - SPAのルーティング
- Material-UI(@mui/material) - ^9.2.0 - SPAのUIコンポーネント

## Infrastructure
- なし(クラウドサービス・IaC定義は未検出。ローカル/オンプレミス実行を前提とした構成)

## Build Tools
- Gradle(Gradle Wrapper同梱) - `lib`、`client/gateway`のビルド
- Vite - ^8.1.3 - `client/spa`のビルド/開発サーバ
- npm - `client/spa`のパッケージ管理
- ESLint - ^10.6.0(typescript-eslint、eslint-plugin-react-hooks、eslint-plugin-react-refresh併用) - `client/spa`のLint

## Testing Tools
- JUnit Jupiter - `lib`の単体テストフレームワーク
- Mockito(mockito-core, mockito-junit-jupiter) - `lib`のモック(inline mocking向けjavaagent設定あり)
- Hamcrest - `lib`のアサーションマッチャ
- spring-boot-starter-test - `lib`のSpring統合テスト基盤

## Other Key Libraries
- GraalVM JS(org.graalvm.js:js, js-scriptengine) - 25.1.3 - スクリプト実行エンジン本体
- tools.jackson.dataformat:jackson-dataformat-yaml(Jackson 3.x系, Spring Boot 4.1.0 BOM管理) - 実行結果のYAMLシリアライズ
- Apache Commons Lang3 / Commons Collections4 - `lib`のユーティリティ

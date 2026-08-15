# demo+クライアント(cli/webconsole)のE2Eテスト追加 - 追加確認質問

前回の回答(C, C[HTTPレベルもPlaywright], A, A, B[新規ディレクトリ+GitHub Actions], B)を踏まえると、以下の技術的な論点がまだ確定していません。ご確認をお願いします。

## Question 1
E2Eテスト実装(Playwright)は、リポジトリのどこに配置しますか？

A) リポジトリ直下に新規`e2e/`ディレクトリを新設する(Gradleモジュールではなく独立したnpmプロジェクト。`@playwright/test`を使用し、demo・webconsole・cliのいずれもここから横断的に扱う)

B) `client/webconsole/frontend`内に追加する(既存のnpm/Viteセットアップに相乗りする)

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
テスト対象(demo・webconsole)のプロセス起動・停止は誰が担いますか？

A) Playwright側(`globalSetup`/`webServer`設定等)で、テスト実行時に自動的にdemo・webconsoleを起動し、終了時に停止する(ローカル実行・CI実行とも同じ手順で完結する)

B) 事前に(開発者またはCIワークフロー側で)demo・webconsoleを起動しておく前提とし、Playwright側では起動・停止を行わない

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 3
cli(`cherry-testtool-cli.jar`)は、E2Eテスト実行のたびにビルドし直しますか？

A) 毎回`./gradlew :client:cli:bootJar`等でビルドし直してから実行する(依存ライブラリの最新化が確実にテストへ反映される。CIワークフローの前段でGradleビルドを行う)

B) 事前にビルド済みのjarがある前提とし、E2Eテスト自体はビルドを行わない

C) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 4
GitHub Actionsのワークフローは、どのタイミングで実行しますか？

A) `main`ブランチへのpush時、およびPull Request作成・更新時に毎回実行する

B) 手動実行(`workflow_dispatch`)のみ(必要な時に開発者が起動する)

C) 定期実行(スケジュール、例: 毎週)のみ

D) Other (please describe after [Answer]: tag below)

[Answer]: C(A+B。push/PR時の自動実行に加え、workflow_dispatchによる手動実行も可能にする)

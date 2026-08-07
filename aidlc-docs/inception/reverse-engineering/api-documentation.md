# API Documentation

すべてのAPIは`lib`モジュール(`cherry.testtool.web`パッケージ)が提供するREST APIであり、SPA・CLIから利用される。`@RequestMapping`にHTTPメソッド指定がないため、実装上はGET/POSTいずれでもアクセス可能だが、SPA/CLIは一貫してPOST(`application/x-www-form-urlencoded`)で呼び出している。以下はその実利用形態に基づく記述。

## REST APIs

### メソッド呼出し実行
- **Method**: POST(実装上は制限なし)
- **Path**: `/testtool/invoker/invoke`
- **Purpose**: 指定クラス・メソッドをリフレクションで呼び出し、結果をYAML文字列で返す
- **Request**: フォームパラメータ — `beanName`(任意)、`className`(必須)、`methodName`(必須)、`methodIndex`(既定0)、`script`(必須、引数生成用JS)、`engine`(任意、既定はデフォルトスクリプトエンジン)
- **Response**: 実行結果をYAML形式でシリアライズした文字列。例外発生時は`ToMapUtil.fromThrowable`による`type`/`message`/`stackTrace`/`cause`を含むYAML文字列

### Bean名解決
- **Method**: POST
- **Path**: `/testtool/invoker/bean`
- **Purpose**: 指定クラスに対応するSpring Bean名一覧を取得
- **Request**: `className`(必須)
- **Response**: Bean名の文字列配列(JSON)。クラスが見つからない場合は空配列

### メソッド解決
- **Method**: POST
- **Path**: `/testtool/invoker/method`
- **Purpose**: 指定クラス・メソッド名に一致する(オーバーロード含む)メソッドシグネチャ一覧を取得
- **Request**: `className`(必須)、`methodName`(必須)
- **Response**: メソッドシグネチャ文字列("戻り値なし、宣言クラスなし、メソッド名なし、引数型あり"形式)の配列

### スタブ登録/解除
- **Method**: POST
- **Path**: `/testtool/stubconfig/put`
- **Purpose**: 指定メソッドにスタブ(戻り値生成スクリプト)を登録、または`script`が空の場合は解除
- **Request**: `className`(必須)、`methodName`(必須)、`methodIndex`(既定0)、`script`(必須、空文字で解除)、`engine`(必須、空文字可)
- **Response**: 登録/解除成功時`"true"`、対象メソッド未検出時`"false"`、クラス未検出時は例外メッセージ文字列

### スタブ参照
- **Method**: POST
- **Path**: `/testtool/stubconfig/get`
- **Purpose**: 登録済みスタブのスクリプト・エンジンと、現在のスクリプト評価結果を取得
- **Request**: `className`(必須)、`methodName`(必須)、`methodIndex`(既定0)
- **Response**: `[script, engine, evaluatedResultOrError]`の3要素配列(JSON)。未登録/未検出時は空文字の配列

### スタブ用Bean名解決
- **Method**: POST
- **Path**: `/testtool/stubconfig/bean`
- **Purpose**: `/testtool/invoker/bean`と同等(スタブ設定画面向け)
- **Request/Response**: `/testtool/invoker/bean`と同一

### スタブ用メソッド解決
- **Method**: POST
- **Path**: `/testtool/stubconfig/method`
- **Purpose**: `/testtool/invoker/method`と同等(スタブ設定画面向け)
- **Request/Response**: `/testtool/invoker/method`と同一

### スタブ一覧
- **Method**: POST
- **Path**: `/testtool/stubconfig/list`
- **Purpose**: 登録済みスタブの対象メソッド一覧を取得(`className`指定時はそのクラスに絞込み)
- **Request**: `className`(任意、空文字で全件)
- **Response**: メソッドシグネチャ文字列(戻り値・宣言クラス・メソッド名・引数型を含む完全形式)の配列

## Internal APIs

### InvokerService
- **Methods**:
  - `String invoke(String beanName, Class<?> beanClass, Method method, String script, String engine)`
  - `String invoke(String beanName, String className, String methodName, int methodIndex, String script, String engine)`
- **Parameters**: Bean名(任意)、対象クラス/メソッド情報、引数生成スクリプト、スクリプトエンジン名
- **Return Types**: 実行結果または例外情報をYAML化した文字列

### ReflectionResolver
- **Methods**: `List<String> resolveBeanName(Class<?>)`、`List<String> resolveBeanName(String)`(default)、`List<Method> resolveMethod(Class<?>, String)`、`List<Method> resolveMethod(String, String)`(default)
- **Parameters**: クラス(またはFQCN文字列)、メソッド名
- **Return Types**: Bean名リスト、Methodリスト

### ScriptProcessor
- **Methods**: `<T> T eval(String script, String engine, Object... args)`
- **Parameters**: スクリプト本文、エンジン名(null可)、可変長引数(スタブ実行時のみ使用)
- **Return Types**: スクリプト評価結果(任意型)

### StubRepository
- **Methods**: `getStubbedMethod()`、`contains(Method)`、`clear(Method)`、`get(Method)`、`put(Method, StubConfig)`
- **Parameters**: `Method`、`StubConfig`
- **Return Types**: `List<Method>`、`boolean`、`StubConfig`(nullable)、`void`

### StubResolver
- **Methods**: `getStubInvocation(Method)`、`getStubInvocation(MethodInvocation)`(default)、`getStubInvocation(ProceedingJoinPoint)`(default)
- **Parameters**: `Method`、AOP Alliance `MethodInvocation`、AspectJ `ProceedingJoinPoint`
- **Return Types**: `Optional<StubInvocation>`

## Data Models

### StubConfig(record)
- **Fields**: `script`(String, 必須) — 戻り値生成スクリプト、`engine`(String, 任意) — スクリプトエンジン名
- **Relationships**: `StubRepository`が`Method`をキーとして保持
- **Validation**: なし(呼出し元Controller層で空文字判定によるクリア処理のみ)

### 永続データモデルなし
本システムはドメインの永続データモデルを持たない。`StubRepositoryImpl`はインメモリの`Map<Method, StubConfig>`のみで状態を保持する。

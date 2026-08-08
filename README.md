
# cherry-testtool

A dynamic testing tool for Spring Boot applications that provides method invocation and stubbing capabilities with JavaScript integration.

## Overview

cherry-testtool is a comprehensive testing framework designed to enhance the testing experience for Spring Boot applications. It enables developers to dynamically invoke methods, configure stubs, and execute JavaScript-based test scenarios through both web interface and command-line tools.

## Features

- **Dynamic Method Invocation**: Invoke Spring Bean methods dynamically using reflection
- **AOP-based Stubbing**: Intercept and mock method calls with customizable return values
- **JavaScript Integration**: Use GraalVM JavaScript engine for argument generation and stub configuration
- **Web Console**: Interactive React SPA (served together with its API proxy) for visual testing and configuration
- **CLI Tools**: Command-line utilities for automated testing workflows
- **Demo Application**: Minimal reference application embedding `lib`, used as the target for `webconsole`/`cli` and as a guide for integrating `lib` into your own application

## Architecture

The project follows a multi-module architecture:

```
cherry-testtool/
├── lib/                    # Core Spring Boot library
│   ├── invoker/           # Dynamic method invocation services
│   ├── stub/              # AOP-based stubbing system
│   ├── script/            # GraalVM JavaScript engine integration
│   ├── reflect/           # Reflection utilities for Spring Bean resolution
│   └── web/               # REST API controller (TesttoolController)
├── demo/                   # Reference application embedding lib (port 8080)
├── client/
│   ├── webconsole/        # SPA + API proxy (Spring Cloud Gateway Server MVC), port 9090
│   │   └── frontend/      # React web interface
│   └── cli/                # Command-line tools
```

## Technology Stack

- **Backend**: Java 25, Spring Boot 4.1.0, Spring AOP, GraalVM JavaScript
- **Frontend**: React 19, TypeScript, Vite, Material-UI
- **Build Tools**: Gradle (Java), npm (JavaScript)
- **Testing**: JUnit 5, Mockito, Hamcrest

## Getting Started

### Prerequisites

- Java 25 or higher
- Node.js 18 or higher
- npm or yarn

### Building the Project

#### Core Library
```bash
cd lib
./gradlew build
```

#### Demo Application
```bash
cd demo
./gradlew build
```

#### Web Console (SPA + API proxy)
```bash
cd client/webconsole
./gradlew build
```

### Running the Application

#### Start Demo Application (target backend)
```bash
cd demo
./gradlew bootRun
```

#### Start Web Console
```bash
cd client/webconsole
./gradlew bootRun
```

#### Start React Development Server (frontend only)
```bash
cd client/webconsole/frontend
npm install
npm run dev
```

### Testing

#### Run Java Tests
```bash
cd lib
./gradlew test
```

#### Lint React Code
```bash
cd client/webconsole/frontend
npm run lint
```

## Usage

### Web Interface

Access the web interface at `http://localhost:5173` (Vite dev server) or `http://localhost:9090` (webconsole, serving both the SPA and its API proxy).

#### Method Invocation
1. Navigate to `/invoker`
2. Specify the target class FQCN
3. Select the bean name (optional)
4. Choose the method to invoke
5. Write JavaScript code to generate method arguments
6. Execute and view results

#### Stub Configuration
1. Navigate to `/stubconfig`
2. Select the target method
3. Define the stub behavior using JavaScript
4. Configure return values and exception handling

### Command Line Interface

Use the CLI (Spring Boot + Picocli) for automated testing:

```bash
cd client/cli && ./gradlew bootJar

# Method invocation
java -jar build/libs/cherry-testtool-cli.jar invoke {DIR}...

# Stub configuration
java -jar build/libs/cherry-testtool-cli.jar stubconfig register|clear|show {DIR}...
```

## Core Components

### InvokerService
Provides dynamic method invocation capabilities using reflection:
- Resolves Spring Beans by class name
- Generates method arguments via JavaScript
- Executes methods and returns formatted results

### StubRepository & StubResolver
Implements AOP-based method stubbing:
- Intercepts method calls using Spring AOP
- Evaluates JavaScript expressions for return values
- Supports conditional stubbing based on method parameters

### ScriptProcessor
Integrates GraalVM JavaScript engine:
- Executes JavaScript code in a secure context
- Provides access to Spring application context
- Supports both argument generation and stub configuration

### ReflectionResolver
Utility for Spring Bean and method resolution:
- Discovers beans by class name
- Resolves overloaded methods with parameter information
- Provides method signature descriptions

## Configuration

The tool supports configuration through Spring Boot properties:

```properties
# Enable/disable the REST API (TesttoolController), enabled by default
cherry.testtool.web.enabled=true

# GraalVM JavaScript engine settings
polyglot.engine.WarnInterpreterOnly=false
```

## JavaScript API

### Method Invocation Scripts
Generate method arguments as arrays:

```javascript
// Simple argument generation
["arg1", 42, true]

// Complex object creation
[
  {
    name: "test",
    value: new Date().getTime()
  }
]
```

### Stub Configuration Scripts
Define return values and behaviors:

```javascript
// Static return value
"stubbed result"

// Dynamic return based on arguments
function(args) {
  return args[0] + " processed";
}

// Conditional stubbing
args[0] === "test" ? "success" : "failure"
```

## Development

### Project Structure
- `lib/src/main/java/cherry/testtool/` - Core implementation
- `demo/` - Reference application embedding `lib`
- `client/webconsole/frontend/src/` - React components and pages
- `client/webconsole/src/` - API proxy / SPA hosting configuration
- `client/cli/src/` - Command-line application (Picocli)

### Adding New Features
1. Implement core logic in the `lib` module
2. Add REST endpoints in web controllers
3. Create corresponding React components
4. Update CLI tools if needed

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

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
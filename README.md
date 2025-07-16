# cherry-testtool

A dynamic testing tool for Spring Boot applications that provides method invocation and stubbing capabilities with JavaScript integration.

## Overview

cherry-testtool is a comprehensive testing framework designed to enhance the testing experience for Spring Boot applications. It enables developers to dynamically invoke methods, configure stubs, and execute JavaScript-based test scenarios through both web interface and command-line tools.

## Features

- **Dynamic Method Invocation**: Invoke Spring Bean methods dynamically using reflection
- **AOP-based Stubbing**: Intercept and mock method calls with customizable return values
- **JavaScript Integration**: Use GraalVM JavaScript engine for argument generation and stub configuration
- **Web Interface**: Interactive React SPA for visual testing and configuration
- **CLI Tools**: Command-line utilities for automated testing workflows
- **Gateway Service**: Spring Cloud Gateway for API routing and service orchestration

## Architecture

The project follows a multi-module architecture:

```
cherry-testtool/
├── lib/                    # Core Spring Boot library
│   ├── invoker/           # Dynamic method invocation services
│   ├── stub/              # AOP-based stubbing system
│   ├── script/            # GraalVM JavaScript engine integration
│   ├── reflect/           # Reflection utilities for Spring Bean resolution
│   └── web/               # REST API controllers
├── client/
│   ├── gateway/           # Spring Cloud Gateway service
│   ├── spa/               # React web interface
│   └── cli/               # Command-line tools
```

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.5.3, Spring AOP, GraalVM JavaScript
- **Frontend**: React 19, TypeScript, Vite, Material-UI
- **Build Tools**: Gradle (Java), npm (JavaScript)
- **Testing**: JUnit 5, Mockito, Hamcrest

## Getting Started

### Prerequisites

- Java 21 or higher
- Node.js 18 or higher
- npm or yarn

### Building the Project

#### Core Library
```bash
cd lib
./gradlew build
```

#### Gateway Service
```bash
cd client/gateway
./gradlew build
```

#### React SPA
```bash
cd client/spa
npm install
npm run build
```

### Running the Application

#### Start Gateway Service
```bash
cd client/gateway
./gradlew bootRun
```

#### Start React Development Server
```bash
cd client/spa
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
cd client/spa
npm run lint
```

## Usage

### Web Interface

Access the web interface at `http://localhost:3000` (development) or through the gateway service.

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

Use the CLI tools for automated testing:

```bash
# Method invocation
./client/cli/invoker.sh

# Stub configuration
./client/cli/stubconfig.sh
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
# Enable/disable web controllers
cherry.testtool.web.invoker=true
cherry.testtool.web.stubconfig=true

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
invocation.arguments[0] === "test" ? "success" : "failure"
```

## Development

### Project Structure
- `lib/src/main/java/cherry/testtool/` - Core implementation
- `client/spa/src/` - React components and pages
- `client/gateway/src/` - Gateway service configuration
- `client/cli/` - Command-line scripts

### Adding New Features
1. Implement core logic in the `lib` module
2. Add REST endpoints in web controllers
3. Create corresponding React components
4. Update CLI tools if needed

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Copyright

Copyright 2019,2025 agwlvssainokuni

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
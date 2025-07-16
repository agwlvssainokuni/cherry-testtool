# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

cherry-testtool is a Java-based testing tool with multi-module architecture consisting of:
- **Core library** (`lib/`) - Spring Boot library providing method invocation and stub configuration services
- **Gateway service** (`client/gateway/`) - Spring Cloud Gateway service for API routing  
- **React SPA** (`client/spa/`) - Web interface for tool interaction
- **CLI tools** (`client/cli/`) - Command-line utilities for invoker and stub configuration

## Build and Development Commands

### Java Components (lib and gateway)
```bash
# Build the core library
cd lib && ./gradlew build

# Build the gateway service  
cd client/gateway && ./gradlew build

# Run tests
cd lib && ./gradlew test
cd client/gateway && ./gradlew test

# Run gateway service
cd client/gateway && ./gradlew bootRun
```

### React SPA
```bash
# Install dependencies
cd client/spa && npm install

# Development server
cd client/spa && npm run dev

# Build for production
cd client/spa && npm run build

# Lint code
cd client/spa && npm run lint

# Clean build artifacts
cd client/spa && npm run clean
```

## Architecture

### Core Library (`lib/`)
- **TesttoolConfiguration** - Spring configuration defining all service beans
- **InvokerService** - Service for dynamically invoking methods on Spring beans via reflection
- **StubResolver/StubRepository** - AOP-based stubbing system for method interception  
- **ReflectionResolver** - Utility for resolving Spring beans and methods by name
- **ScriptProcessor** - GraalVM JS engine integration for script execution
- **Web Controllers** - REST endpoints for invoker and stub configuration

### Gateway Service (`client/gateway/`)
- Spring Cloud Gateway application serving as API gateway
- Main class: `cherry.testtool.gateway.Main`
- Configured to route requests to appropriate services

### React SPA (`client/spa/`)
- Built with React 19, TypeScript, Vite, and Material-UI
- Two main modules:
  - **Invoker** (`/invoker`) - UI for method invocation with script-based argument generation
  - **Stub Config** (`/stubconfig`) - UI for configuring method stubs
- Uses React Router for navigation

## Key Technologies

- **Java 21** with Spring Boot 3.5.3
- **GraalVM JavaScript** engine for script execution
- **Spring AOP** for method interception
- **React 19** with TypeScript
- **Vite** for build tooling
- **Material-UI** for React components
- **Gradle** for Java build management
- **npm** for JavaScript package management

## Development Notes

- All Java code uses Java 21 language features
- React components use functional components with hooks
- Scripts are executed using GraalVM JS engine
- Stub configurations support JavaScript for return value generation
- Web interface provides interactive method invocation and stub management
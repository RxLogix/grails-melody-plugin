# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Grails plugin that integrates [JavaMelody](https://github.com/javamelody/javamelody/wiki) monitoring into Grails applications. Once installed, the host app exposes a monitoring dashboard at `/<context>/monitoring`. This branch (`6.x-upgrade`) targets **Grails 6.2.0 / Java 11** and is published by RxLogix (fork of the upstream `sergiomichels/grails-melody-plugin`).

## Repository layout

This is a **multi-project Gradle build** (`settings.gradle`) with two modules:

- **`plugin/`** — the actual plugin (project name `grails-melody-plugin`). All plugin source lives here.
- **`app-integration-tests/`** — a full standalone Grails app that depends on `project(':grails-melody-plugin')` and exists solely to run integration tests against the plugin.

The plugin's Groovy source is under `plugin/src/main/groovy/`, split across two packages:
- `grails.melody.plugin` — the plugin descriptor, config, and utility.
- `net.bull.javamelody` — `MelodyInterceptorEnhancer`, deliberately placed in JavaMelody's own package so it can access package-private JavaMelody internals (`MonitoringProxy.getSpringCounter()`, `Parameter`, etc.).

## Build & test commands

Run from the repo root (the wrapper is Gradle 7.6.4):

```bash
# Build only the plugin
./gradlew :grails-melody-plugin:build

# Assemble the plugin jar (bootJar is disabled; a plain plugin jar is produced)
./gradlew :grails-melody-plugin:jar

# Install the plugin to the local Maven repo so the test app / other apps can resolve it
./gradlew :grails-melody-plugin:publishToMavenLocal

# Run the integration tests (spins up the full test app + H2)
./gradlew :app-integration-tests:integrationTest

# Run a single integration test
./gradlew :app-integration-tests:integrationTest --tests "grails.melody.plugin.GrailsTransactionSpec"
```

Tests use JUnit Platform (`useJUnitPlatform()`) with Spock. Integration tests are `@Integration` specs that boot the test app's `Application` class.

## Publishing

`plugin/build.gradle` defines two publish targets, both credential-driven (never hardcode these):
- **GitHubPackages** — `maven.pkg.github.com/RxLogix/grails-melody-plugin`, uses `gpr.user`/`gpr.key` properties or `GITHUB_USERNAME`/`GITHUB_TOKEN` env vars.
- **NexusRepo** — uses `nexusUsername`/`nexusPassword` (or `NEXUS_*` env vars) and `nexusUrl`.

The plugin version and the JavaMelody dependency version are centralized in `gradle.properties` (`version`, `versionDependency`).

## How the plugin works (architecture)

The plugin has no controllers of its own — the `/monitoring` UI is served entirely by JavaMelody's servlet filter. The plugin's job is to wire JavaMelody into the Grails/Spring lifecycle. Three moving parts, all driven from `GrailsMelodyPluginGrailsPlugin.groovy`:

1. **Servlet filter registration** (`MelodyConfig`) — `doWithSpring()` registers the `melodyConfig` bean. `MelodyConfig` contributes two Spring beans: a `ServletContextInitializer` that adds JavaMelody's `SessionListener`, and a `FilterRegistrationBean` for JavaMelody's `MonitoringFilter` mapped to `/*`. All `javamelody.*` config keys are read and passed through as filter init-parameters.

2. **DataSource wrapping** (`doWithApplicationContext`) — wraps the app's SQL `DataSource` in JavaMelody's `JdbcWrapper` proxy so queries are monitored. This is done here (not via a `BeanPostProcessor`, which "didn't work"). It handles both older lazy-proxied datasources and the modern `hibernateDatastore` transaction-manager datasource (including `DelegatingDataSource`). **This is the fragile part** — recent commits (`Fix melody config merge issue`) reflect breakage around datasource/config wiring, so be careful here.

3. **Service method interception** (`MelodyInterceptorEnhancer`, called from `doWithDynamicMethods`) — uses Groovy metaprogramming to override `metaClass.invokeMethod` on every Grails service class, binding a JavaMelody `SPRING_COUNTER` around each call so per-service-method stats are collected. This requires `ExpandoMetaClass.enableGlobally()`, which is invoked in a static initializer **except in the TEST environment** (enabling it globally breaks unit tests).

### Configuration resolution

`GrailsMelodyUtil.getGrailsMelodyConfig()` returns the merged config. It starts from `application.config` (i.e. the host app's `application.yml` under the `javamelody:` key) and then attempts to merge an optional external `GrailsMelodyConfig` Groovy config class (loaded by name via the classloader, environment-aware via `ConfigSlurper`). If that class isn't present the exception is swallowed and defaults are used. All JavaMelody optional parameters (see the JavaMelody UserGuide) are configured under the `javamelody:` key in the host app's `application.yml` — e.g. `disabled`, `http-transform-pattern`, `sql-transform-pattern`, `authorized-users`.

## Important constraints

- **Java 11** source/target compatibility is set explicitly in both `plugin/build.gradle` and `app-integration-tests/build.gradle`. This is a Grails 6 branch — it uses `javax.servlet` (not `jakarta.servlet`). A move to Grails 7 would require the `javax`→`jakarta` namespace migration.
- JavaMelody core is an `implementation` dependency (bundled); Grails/Spring/servlet APIs are `compileOnly` (provided by the host app at runtime).
- The `net.bull.javamelody` package placement of `MelodyInterceptorEnhancer` is intentional — do not "clean it up" into another package.
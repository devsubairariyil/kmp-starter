# Contributing To KMP Starter

Start with [docs/development/engineering-guide.md](docs/development/engineering-guide.md). It is the source of truth for architecture, module ownership, and coding standards.

## Local Setup

Requirements:

- JDK 21
- Android Studio with the Android SDK for the configured compile SDK
- Gradle wrapper files checked into the repository

Common commands:

```bash
./gradlew :core:network:openApiGenerate
./gradlew spotlessCheck
./gradlew detekt ktlintCheck
./gradlew test
./gradlew lint
./gradlew :app:assembleNonProdDebug
```

If the wrapper jar is missing, bootstrap it once with a system Gradle install:

```bash
gradle wrapper --gradle-version 9.4.1
```

Install the repository Git hooks once per clone:

```bash
git config core.hooksPath .githooks
```

The pre-commit hook runs formatting and static analysis checks. The pre-push hook runs OpenAPI generation, tests, lint, and the non-production debug assemble. For emergency local bypass only, run the Git command with `SKIP_KMP_STARTER_HOOKS=1`; CI still runs the full pull request gate.

## Configuration

API base URLs are supplied per product flavor through Gradle properties, environment variables, or `local.properties`:

```properties
NON_PROD_API_BASE_URL=https://api.nonprod.example.com/
PROD_API_BASE_URL=https://api.example.com/
```

Release signing values must also come from Gradle properties, environment variables, or `local.properties`:

```properties
KEYSTORE_FILE=path/to/release-keystore.jks
KEYSTORE_PASSWORD=your-keystore-password
KEY_ALIAS=your-key-alias
KEY_PASSWORD=your-key-password
```

For local development, put those values in the untracked `local.properties` file. `KEYSTORE_FILE` should point to a local keystore file that is also not committed. For CI/CD, store the same four values as encrypted secrets and expose them to Gradle as environment variables or Gradle properties during release jobs.

Do not commit secrets, keystores, tokens, or machine-specific SDK paths.

## Pull Request Checklist

- Install the repo hooks with `git config core.hooksPath .githooks`.
- Run the relevant Gradle checks before committing code changes: `spotlessCheck`, `detekt ktlintCheck`, `test`, `lint`, and `:app:assembleNonProdDebug`.
- Pull requests run the same checks in GitHub Actions. A PR must not be merged if the `Android / verify` workflow job is failing.
- Code follows the module boundaries in the engineering guide.
- New behavior has tests or a clear reason tests are not practical yet.
- Generated OpenAPI code has not been manually edited.
- Generated OpenAPI code remains under `core/network/build/generated/openapi`.
- New dependencies are scoped to the narrowest module.
- UI observes ViewModel state and does not call data sources directly.
- Offline-first data still flows through Room.
- `README.md` or docs are updated for new setup or architectural decisions.
- Relevant Gradle checks have been run when the wrapper is available.

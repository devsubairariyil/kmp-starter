#!/bin/sh

APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$JAR" ]; then
  exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Gradle wrapper jar is missing and no system gradle command was found." >&2
echo "Install Gradle once and run: gradle wrapper --gradle-version 9.4.1" >&2
exit 1

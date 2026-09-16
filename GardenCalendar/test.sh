#!/usr/bin/env bash
# JVM-проверка логики (правила, планировщик, расчёт материалов) на реальном коде приложения.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
OUT="$ROOT/out-test"
rm -rf "$OUT" && mkdir -p "$OUT"

JSON_JAR="/home/user/tools/lib/json.jar"

javac -nowarn -encoding UTF-8 -d "$OUT" \
  -classpath "$JSON_JAR" \
  -sourcepath "$ROOT/test/stubs:$ROOT/test:$ROOT/src" \
  "$ROOT/test/LogicTest.java"

java -Dfile.encoding=UTF-8 -cp "$OUT:$JSON_JAR" by.csl.gardener.LogicTest

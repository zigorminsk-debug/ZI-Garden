#!/usr/bin/env bash
# JVM-проверка логики (правила, планировщик, расчёт материалов) на реальном коде приложения.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
# тест ищет res/drawable-nodpi по относительному пути — всегда работаем из каталога проекта
cd "$ROOT"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
if [ ! -x "$JAVA_HOME/bin/javac" ]; then
  JAVA_HOME="$( (ls -d /usr/lib/jvm/java-21-openjdk-amd64 /home/user/tools/jdk-17* /home/user/ZI-Garden/tools/jdk-17* 2>/dev/null || true) | head -1 )"
fi
if [ ! -x "$JAVA_HOME/bin/javac" ]; then
  JAVAC_BIN="$(command -v javac || true)"
  [ -n "$JAVAC_BIN" ] && JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$JAVAC_BIN")")")"
fi
[ -x "$JAVA_HOME/bin/javac" ] || { echo "ОШИБКА: не найден javac — установите JDK 17+"; exit 1; }
export PATH="$JAVA_HOME/bin:$PATH"
OUT="$ROOT/out-test"
rm -rf "$OUT" && mkdir -p "$OUT"

# json.jar: из переменной окружения, иначе ищем в репозитории и в старых путях машины разработчика
JSON_JAR="${JSON_JAR:-}"
if [ -z "$JSON_JAR" ]; then
  for c in "$ROOT/../tools/lib/json.jar" "/home/user/tools/lib/json.jar" "/home/user/ZI-Garden/tools/lib/json.jar"; do
    if [ -f "$c" ]; then JSON_JAR="$c"; break; fi
  done
fi
[ -f "$JSON_JAR" ] || { echo "ОШИБКА: не найден json.jar (задайте JSON_JAR)"; exit 1; }

javac -nowarn -encoding UTF-8 -d "$OUT" \
  -classpath "$JSON_JAR" \
  -sourcepath "$ROOT/test/stubs:$ROOT/test:$ROOT/src" \
  "$ROOT/test/LogicTest.java" 2>"$OUT/javac.err" \
  || { cat "$OUT/javac.err"; \
       while IFS= read -r line; do [ -n "$line" ] && echo "::error::javac: $line"; done < "$OUT/javac.err"; \
       exit 1; }

set +e
java -Dfile.encoding=UTF-8 -cp "$OUT:$JSON_JAR" by.csl.gardener.LogicTest 2>"$OUT/java.err"
JAVA_RC=$?
set -e
[ -s "$OUT/java.err" ] && cat "$OUT/java.err" >&2
if [ "$JAVA_RC" -ne 0 ]; then
  while IFS= read -r line; do [ -n "$line" ] && echo "::error::test: $line"; done < "$OUT/java.err"
  exit "$JAVA_RC"
fi

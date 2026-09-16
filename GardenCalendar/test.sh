#!/usr/bin/env bash
# JVM-проверка логики (правила, планировщик, расчёт материалов) на реальном коде приложения.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
# тест ищет res/drawable-nodpi по относительному пути — всегда работаем из каталога проекта
cd "$ROOT"
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}
[ -x "$JAVA_HOME/bin/javac" ] || JAVA_HOME="$(ls -d /usr/lib/jvm/java-21-openjdk-amd64 /home/user/tools/jdk-17* /home/user/ZI-Garden/tools/jdk-17* 2>/dev/null | head -1)"
[ -x "$JAVA_HOME/bin/javac" ] || JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
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
  "$ROOT/test/LogicTest.java"

java -Dfile.encoding=UTF-8 -cp "$OUT:$JSON_JAR" by.csl.gardener.LogicTest

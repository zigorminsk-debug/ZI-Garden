#!/usr/bin/env bash
# Сборка APK "Календарь садовода" без Gradle: aapt2 + javac + d8 + apksigner
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK="${ANDROID_HOME:-/home/user/android-sdk}"
PLATFORM="$SDK/platforms/android-34"
BT="$SDK/build-tools/34.0.0"
# JAVA_HOME: уважаем уже заданный (например, actions/setup-java в CI),
# иначе ищем типовые установки; fallback'и не должны валить скрипт при set -euo pipefail
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
if [ ! -x "$JAVA_HOME/bin/javac" ]; then
  JAVA_HOME="$( (ls -d /usr/lib/jvm/java-21-openjdk-amd64 /home/user/tools/jdk-17* /home/user/ZI-Garden/tools/jdk-17* 2>/dev/null || true) | head -1 )"
fi
if [ ! -x "$JAVA_HOME/bin/javac" ]; then
  JAVAC_BIN="$(command -v javac || true)"
  [ -n "$JAVAC_BIN" ] && JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$JAVAC_BIN")")")"
fi
[ -x "$JAVA_HOME/bin/javac" ] || { echo "ОШИБКА: не найден javac — установите JDK 17+"; exit 1; }
export PATH="$JAVA_HOME/bin:$BT:$PATH"

OUT="$ROOT/out"
GEN="$OUT/gen"
OBJ="$OUT/classes"
rm -rf "$OUT"
mkdir -p "$GEN" "$OBJ" "$OUT/apk" "$OUT/dex"

echo "[1/8] aapt2 compile"
aapt2 compile --dir "$ROOT/res" -o "$OUT/res.zip"

# Версию можно переопределить извне (CI подставляет автономер);
# при локальной сборке остаются значения по умолчанию.
APP_VERSION_CODE="${APP_VERSION_CODE:-17}"
APP_VERSION_NAME="${APP_VERSION_NAME:-2.5}"
echo "Версия APK: $APP_VERSION_NAME (code $APP_VERSION_CODE)"

echo "[2/8] aapt2 link"
aapt2 link -o "$OUT/app.unsigned.apk" \
  -I "$PLATFORM/android.jar" \
  --manifest "$ROOT/AndroidManifest.xml" \
  --java "$GEN" \
  --auto-add-overlay \
  --min-sdk-version 24 --target-sdk-version 34 \
  --version-code "$APP_VERSION_CODE" --version-name "$APP_VERSION_NAME" \
  "$OUT/res.zip"

echo "[3/8] javac"
find "$ROOT/src" "$GEN" -name '*.java' > "$OUT/sources.txt"
javac --release 8 -nowarn -encoding UTF-8 \
  -sourcepath "$ROOT/src:$GEN" \
  -classpath "$PLATFORM/android.jar" \
  -d "$OBJ" @"$OUT/sources.txt"

echo "[4/8] strip MethodParameters (JDK 21 javac emits null names -> d8 NPE)"
python3 "$ROOT/tools/strip_method_params.py" $(find "$OBJ" -name '*.class')

echo "[5/8] d8 -> classes.dex"
find "$OBJ" -name '*.class' > "$OUT/cls.txt"
d8 --release --lib "$PLATFORM/android.jar" --min-api 24 \
  --output "$OUT/dex" $(cat "$OUT/cls.txt")

echo "[6/8] zip classes.dex + zipalign"
cd "$OUT/dex" && zip -q -0 ../app.unsigned.apk classes.dex && cd "$ROOT"
zipalign -f -p 4 "$OUT/app.unsigned.apk" "$OUT/app.aligned.apk"

echo "[7/8] sign"
KS="$ROOT/release.keystore"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -v -keystore "$KS" -alias csl \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass cslgarden -keypass cslgarden \
    -dname "CN=Zakharevich Igor, OU=csl.by, O=CSL, L=Minsk, C=BY" >/dev/null 2>&1
fi
apksigner sign --ks "$KS" --ks-pass pass:cslgarden --key-pass pass:cslgarden \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out "$OUT/ZI-Garden-csl.by-v2.5.apk" "$OUT/app.aligned.apk"

echo "[8/8] verify"
apksigner verify --print-certs "$OUT/ZI-Garden-csl.by-v2.5.apk" > "$OUT/verify.txt" || exit 1
head -5 "$OUT/verify.txt"
"$BT/aapt2" dump badging "$OUT/ZI-Garden-csl.by-v2.5.apk" > "$OUT/badging.txt" || exit 1
head -8 "$OUT/badging.txt"
ls -lh "$OUT/ZI-Garden-csl.by-v2.5.apk"
echo "OK: $OUT/ZI-Garden-csl.by-v2.5.apk"

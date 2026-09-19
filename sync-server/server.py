#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ZI Garden — минимальный сервер семейной синхронизации.

Что умеет:
  POST /api/family/create   {"family": "Ивановы", "login": "papa", "password": "..."}
  POST /api/family/join     {"family": "Ивановы", "login": "mama", "password": "..."}
  POST /api/state/push      {"token": "...", "ts": 1695..., "json": "<резервная копия текстом>"}
  GET  /api/state/pull?token=...
  GET  /api/health

Хранение: файлы JSON в каталоге data/ (атомарная запись через rename).
Пароли: PBKDF2-HMAC-SHA256, 200k итераций. Токены: случайные, хранятся хэшем SHA-256.
Синхронизация: целиком, кто последний прислал — того и данные (ts клиента должен быть новее серверного).

Запуск:  python3 server.py [--port 8080] [--data ./data]
Деплой:  любой бесплатный хост для Python (см. README.md).
"""

import hashlib
import json
import os
import re
import secrets
import sys
import tempfile
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs

MAX_BODY = 3 * 1024 * 1024          # тело запроса до 3 МБ (копия данных с запасом)
LOGIN_RE = re.compile(r"^[a-zA-Z0-9_\-]{3,32}$")
FAMILY_RE = re.compile(r"^[\w\-]{2,40}$", re.UNICODE)
PBKDF2_ITER = 200_000

_lock = threading.Lock()


def canon(name):
    """Имя семьи → идентификатор каталога (безопасный)."""
    slug = re.sub(r"[^\w\-]+", "-", name.strip().lower()).strip("-")
    return slug[:40] or "family"


def now_ms():
    return int(time.time() * 1000)


class Store:
    """Файловое хранилище состояний семей."""

    def __init__(self, root):
        self.root = os.path.abspath(root)
        os.makedirs(self.root, exist_ok=True)

    def _fam_dir(self, fam_slug):
        d = os.path.join(self.root, fam_slug)
        return d

    def _read(self, path, default=None):
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return default

    def _write(self, path, obj):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        fd, tmp = tempfile.mkstemp(dir=os.path.dirname(path), suffix=".tmp")
        with os.fdopen(fd, "w", encoding="utf-8") as f:
            json.dump(obj, f, ensure_ascii=False)
        os.replace(tmp, path)

    # ── семьи и участники ──────────────────────────────────────────────
    def family_exists(self, fam_slug):
        return os.path.isdir(self._fam_dir(fam_slug))

    def create_family(self, fam_slug):
        os.makedirs(self._fam_dir(fam_slug), exist_ok=True)

    def members(self, fam_slug):
        return self._read(os.path.join(self._fam_dir(fam_slug), "members.json"), {})

    def save_members(self, fam_slug, members):
        self._write(os.path.join(self._fam_dir(fam_slug), "members.json"), members)

    def state(self, fam_slug):
        return self._read(os.path.join(self._fam_dir(fam_slug), "state.json"))

    def save_state(self, fam_slug, state):
        self._write(os.path.join(self._fam_dir(fam_slug), "state.json"), state)


def hash_password(password, salt_hex):
    return hashlib.pbkdf2_hmac(
        "sha256", password.encode("utf-8"), bytes.fromhex(salt_hex), PBKDF2_ITER).hex()


def hash_token(token):
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def ok_payload():
    return {"ok": True}


class Handler(BaseHTTPRequestHandler):
    server_version = "ZIGardenSync/1.0"
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):  # тихий сервер
        pass

    # ── транспорт ──────────────────────────────────────────────────────
    def _send(self, code, obj):
        body = json.dumps(obj, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def _read_json(self):
        length = int(self.headers.get("Content-Length") or 0)
        if length <= 0 or length > MAX_BODY:
            return None
        raw = self.rfile.read(length)
        try:
            return json.loads(raw.decode("utf-8"))
        except Exception:
            return None

    def _fail(self, code, message):
        self._send(code, {"ok": False, "error": message})

    # ── маршруты ───────────────────────────────────────────────────────
    def do_GET(self):
        url = urlparse(self.path)
        if url.path == "/api/health":
            self._send(200, {"ok": True, "service": "zi-garden-sync", "time": now_ms()})
            return
        if url.path == "/api/state/pull":
            query = parse_qs(url.query)
            self._handle_pull((query.get("token") or [""])[0])
            return
        self._send(404, {"ok": False, "error": "нет такого пути",
                         "hint": "сервер синхронизации ZI Garden",
                         "paths": ["/api/health", "/api/family/create",
                                   "/api/family/join", "/api/state/push", "/api/state/pull"]})

    def do_POST(self):
        url = urlparse(self.path)
        body = self._read_json()
        if body is None:
            return self._fail(400, "тело запроса — не JSON или слишком большое")
        if url.path == "/api/family/create":
            return self._handle_create(body)
        if url.path == "/api/family/join":
            return self._handle_join(body)
        if url.path == "/api/state/push":
            return self._handle_push(body)
        self._fail(404, "нет такого пути")

    # ── авторизация ────────────────────────────────────────────────────
    @staticmethod
    def _valid_creds(family, login, password):
        if not FAMILY_RE.match(family or ""):
            return "имя семьи: 2–40 букв/цифр (например, Ивановы)"
        if not LOGIN_RE.match(login or ""):
            return "логин: 3–32 латинских букв, цифр, _ или -"
        if not password or len(password) < 4:
            return "пароль: минимум 4 символа"
        return None

    def _auth(self, token):
        """token → (fam_slug, login) или None."""
        if not token:
            return None
        th = hash_token(token)
        store = self.server.store
        with _lock:
            for fam_slug in os.listdir(store.root):
                members = store.members(fam_slug)
                for login, m in members.items():
                    if m.get("token_hash") == th:
                        return fam_slug, login
        return None

    def _require_auth(self, token):
        auth = self._auth(token)
        if auth is None:
            self._fail(401, "токен недействителен — войдите заново")
        return auth

    # ── семья ──────────────────────────────────────────────────────────
    def _issue(self, store, fam_slug, login):
        """Выдать токен участнику и вернуть его."""
        members = store.members(fam_slug)
        token = secrets.token_hex(24)
        m = members[login]
        m["token_hash"] = hash_token(token)
        m["last_seen"] = now_ms()
        store.save_members(fam_slug, members)
        return token

    def _handle_create(self, body):
        family = str(body.get("family") or "").strip()
        login = str(body.get("login") or "").strip()
        password = str(body.get("password") or "")
        bad = self._valid_creds(family, login, password)
        if bad:
            return self._fail(400, bad)
        fam_slug = canon(family)
        store = self.server.store
        with _lock:
            if store.family_exists(fam_slug):
                return self._fail(409, "семья с таким именем уже есть — войдите через «Присоединиться»")
            store.create_family(fam_slug)
            salt = secrets.token_hex(16)
            store.save_members(fam_slug, {
                login: {"pwd": hash_password(password, salt), "salt": salt,
                        "created": now_ms(), "role": "owner"}})
            token = self._issue(store, fam_slug, login)
        self._send(200, {"ok": True, "family": fam_slug, "login": login, "token": token,
                         "hint": "передайте родным имя семьи и ваш пароль — они присоединятся своим логином"})

    def _handle_join(self, body):
        family = str(body.get("family") or "").strip()
        login = str(body.get("login") or "").strip()
        password = str(body.get("password") or "")
        bad = self._valid_creds(family, login, password)
        if bad:
            return self._fail(400, bad)
        fam_slug = canon(family)
        store = self.server.store
        with _lock:
            if not store.family_exists(fam_slug):
                return self._fail(404, "такой семьи нет — сначала кто-то должен её создать")
            members = store.members(fam_slug)
            member = members.get(login)
            if member is None:
                # новый участник: ему нужен «семейный пароль» — пароль любого существующего члена
                known = next(iter(members.values()))
                if hash_password(password, known["salt"]) != known["pwd"]:
                    return self._fail(403, "пароль не подходит ни одному участнику семьи")
                salt = secrets.token_hex(16)
                members[login] = {"pwd": hash_password(password, salt), "salt": salt,
                                  "created": now_ms(), "role": "member"}
                store.save_members(fam_slug, members)
            elif hash_password(password, member["salt"]) != member["pwd"]:
                return self._fail(403, "неверный пароль")
            token = self._issue(store, fam_slug, login)
        self._send(200, {"ok": True, "family": fam_slug, "login": login, "token": token})

    # ── состояние ──────────────────────────────────────────────────────
    def _handle_push(self, body):
        auth = self._require_auth(str(body.get("token") or ""))
        if auth is None:
            return
        fam_slug, login = auth
        ts = int(body.get("ts") or 0)
        payload = body.get("json")
        if not isinstance(payload, str) or not payload:
            return self._fail(400, "пустое состояние")
        if len(payload) > MAX_BODY:
            return self._fail(413, "состояние слишком большое")
        store = self.server.store
        with _lock:
            current = store.state(fam_slug) or {}
            if int(current.get("ts", 0)) >= ts:
                return self._send(200, {"ok": True, "applied": False, "ts": current.get("ts", 0),
                                        "by": current.get("by", "?"),
                                        "reason": "на сервере уже есть не старее — заберите через pull"})
            store.save_state(fam_slug, {"ts": ts, "by": login, "json": payload, "pushed_at": now_ms()})
        self._send(200, {"ok": True, "applied": True, "ts": ts})

    def _handle_pull(self, token):
        auth = self._require_auth(token)
        if auth is None:
            return
        fam_slug, _login = auth
        store = self.server.store
        with _lock:
            state = store.state(fam_slug)
        if state is None:
            return self._send(200, {"ok": True, "empty": True, "ts": 0})
        self._send(200, {"ok": True, "ts": state.get("ts", 0), "by": state.get("by", "?"),
                         "json": state.get("json", "")})


def main(argv):
    port = 8080
    data = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data")
    args = argv[1:]
    for i, a in enumerate(args):
        if a == "--port" and i + 1 < len(args):
            port = int(args[i + 1])
        if a == "--data" and i + 1 < len(args):
            data = args[i + 1]
    if os.environ.get("PORT"):
        port = int(os.environ["PORT"])
    server = ThreadingHTTPServer(("0.0.0.0", port), Handler)
    server.store = Store(data)
    print(f"ZI Garden sync: порт {port}, данные: {data}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main(sys.argv)

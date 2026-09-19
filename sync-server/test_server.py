#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Автотесты сервера синхронизации: запуск на случайном порту + полный цикл."""

import json
import socket
import shutil
import subprocess
import sys
import tempfile
import time
import unittest
import urllib.request

SERVER = __file__.replace("test_server.py", "server.py")


def free_port():
    s = socket.socket()
    s.bind(("127.0.0.1", 0))
    port = s.getsockname()[1]
    s.close()
    return port


class SyncServerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.port = free_port()
        cls.data = tempfile.mkdtemp(prefix="zigarden-sync-test-")
        cls.proc = subprocess.Popen(
            [sys.executable, SERVER, "--port", str(cls.port), "--data", cls.data],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        cls.base = f"http://127.0.0.1:{cls.port}"
        for _ in range(50):
            try:
                urllib.request.urlopen(cls.base + "/api/health", timeout=1)
                break
            except Exception:
                time.sleep(0.1)

    @classmethod
    def tearDownClass(cls):
        cls.proc.terminate()
        shutil.rmtree(cls.data, ignore_errors=True)

    def post(self, path, obj):
        req = urllib.request.Request(
            self.base + path, data=json.dumps(obj).encode("utf-8"),
            headers={"Content-Type": "application/json"}, method="POST")
        try:
            with urllib.request.urlopen(req, timeout=5) as r:
                return r.status, json.loads(r.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read().decode("utf-8"))

    def get(self, path):
        try:
            with urllib.request.urlopen(self.base + path, timeout=5) as r:
                return r.status, json.loads(r.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read().decode("utf-8"))

    def test_health(self):
        code, body = self.get("/api/health")
        self.assertTrue(body["ok"])

    def test_full_family_cycle(self):
        # создаём семью
        code, body = self.post("/api/family/create", {
            "family": "Ивановы", "login": "papa", "password": "ogorod2026"})
        self.assertEqual(200, code)
        self.assertTrue(body["ok"])
        owner_token = body["token"]
        self.assertEqual("ивановы", body["family"])

        # дубль семьи запрещён
        code, body = self.post("/api/family/create", {
            "family": "Ивановы", "login": "vasya", "password": "xxx123"})
        self.assertEqual(409, code)

        # папа отправляет состояние
        code, body = self.post("/api/state/push", {
            "token": owner_token, "ts": 1001, "json": "{\"fmt\":\"zi-garden-backup\"}"})
        self.assertTrue(body["applied"])

        # мама присоединяется паролем папы (любым действующим)
        code, body = self.post("/api/family/join", {
            "family": "Ивановы", "login": "mama", "password": "ogorod2026"})
        self.assertEqual(200, code)
        mama_token = body["token"]

        # мама читает состояние
        code, body = self.get(f"/api/state/pull?token={mama_token}")
        self.assertEqual(1001, body["ts"])
        self.assertIn("zi-garden-backup", body["json"])
        self.assertEqual("papa", body["by"])

        # мама отвечает более новым состоянием
        code, body = self.post("/api/state/push", {
            "token": mama_token, "ts": 1002, "json": "{\"fmt\":\"zi-garden-backup\",\"new\":1}"})
        self.assertTrue(body["applied"])

        # старое проигрывает: папа не может затереть мамино
        code, body = self.post("/api/state/push", {
            "token": owner_token, "ts": 999, "json": "{}"})
        self.assertFalse(body["applied"])
        self.assertEqual(1002, body["ts"])

        # чужой пароль не пускает нового члена
        code, body = self.post("/api/family/join", {
            "family": "Ивановы", "login": "hacker", "password": "неправильно"})
        self.assertEqual(403, code)

        # битый токен не пускает
        code, body = self.get("/api/state/pull?token=deadbeef")
        self.assertEqual(401, code)

    def test_validation(self):
        code, body = self.post("/api/family/create", {
            "family": "x", "login": "no!", "password": ""})
        self.assertEqual(400, code)
        code, body = self.post("/api/family/join", {
            "family": "Петровы-Нет", "login": "anna", "password": "1234"})
        self.assertEqual(404, code)


if __name__ == "__main__":
    unittest.main(verbosity=2)

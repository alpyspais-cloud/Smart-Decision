import hashlib
import hmac
import json
import os
import re
import secrets
import sqlite3
import time
from http import HTTPStatus
from http.cookies import CookieError, SimpleCookie
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse


ROOT = Path(__file__).resolve().parent
DATABASE = Path(os.environ.get("SMART_DECISION_DB", ROOT / "smartdecision.sqlite3"))
HOST = os.environ.get("SMART_DECISION_HOST", "127.0.0.1")
PORT = int(os.environ.get("SMART_DECISION_PORT", "8765"))
SESSION_SECONDS = 7 * 24 * 60 * 60
PASSWORD_ITERATIONS = 310_000
EMAIL_PATTERN = re.compile(r"^[^@\s]{1,64}@[^@\s.]+(?:\.[^@\s.]+)+$")


def connect_db():
    connection = sqlite3.connect(DATABASE, timeout=10)
    connection.row_factory = sqlite3.Row
    connection.execute("PRAGMA foreign_keys = ON")
    return connection


def init_db():
    DATABASE.parent.mkdir(parents=True, exist_ok=True)
    with connect_db() as connection:
        connection.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password_hash TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """)
        user_columns = {
            row["name"] for row in connection.execute("PRAGMA table_info(users)")
        }
        if "role" not in user_columns:
            connection.execute(
                "ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'user' "
                "CHECK (role IN ('user', 'admin'))"
            )
        connection.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                token_hash TEXT PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                expires_at INTEGER NOT NULL
            )
        """)
        connection.execute("""
            CREATE TABLE IF NOT EXISTS posts (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                media_type TEXT NOT NULL CHECK (media_type IN ('none', 'image', 'video')),
                media_url TEXT NOT NULL DEFAULT '',
                is_sponsored INTEGER NOT NULL DEFAULT 0 CHECK (is_sponsored IN (0, 1)),
                author_id INTEGER NOT NULL REFERENCES users(id),
                created_at INTEGER NOT NULL
            )
        """)
        connection.execute("CREATE INDEX IF NOT EXISTS sessions_expiry ON sessions(expires_at)")
        connection.execute(
            "CREATE INDEX IF NOT EXISTS posts_created ON posts(is_sponsored, created_at DESC)"
        )


def hash_password(password, salt=None):
    salt = salt or secrets.token_bytes(16)
    derived = hashlib.pbkdf2_hmac(
        "sha256", password.encode("utf-8"), salt, PASSWORD_ITERATIONS
    )
    return f"{PASSWORD_ITERATIONS}${salt.hex()}${derived.hex()}"


def verify_password(password, encoded):
    try:
        iterations, salt_hex, expected = encoded.split("$")
        actual = hashlib.pbkdf2_hmac(
            "sha256", password.encode("utf-8"), bytes.fromhex(salt_hex), int(iterations)
        )
        return hmac.compare_digest(actual.hex(), expected)
    except (ValueError, TypeError):
        return False


class SmartDecisionHandler(BaseHTTPRequestHandler):
    server_version = "SmartDecision"

    def end_headers(self):
        self.send_header("X-Content-Type-Options", "nosniff")
        self.send_header("X-Frame-Options", "DENY")
        self.send_header("Referrer-Policy", "same-origin")
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def send_json(self, status, payload, headers=()):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        for key, value in headers:
            self.send_header(key, value)
        self.end_headers()
        self.wfile.write(body)

    def read_json(self):
        try:
            length = int(self.headers.get("Content-Length", "0"))
        except ValueError:
            return None
        if length < 1 or length > 16_384:
            return None
        try:
            data = json.loads(self.rfile.read(length))
        except (json.JSONDecodeError, UnicodeDecodeError):
            return None
        return data if isinstance(data, dict) else None

    def origin_is_same_site(self):
        origin = self.headers.get("Origin")
        if not origin:
            return True
        parsed = urlparse(origin)
        return parsed.netloc.lower() == self.headers.get("Host", "").lower()

    def session_token(self):
        cookie = SimpleCookie()
        try:
            cookie.load(self.headers.get("Cookie", ""))
        except CookieError:
            return None
        morsel = cookie.get("sd_session")
        return morsel.value if morsel else None

    def current_user(self):
        token = self.session_token()
        if not token:
            return None
        token_hash = hashlib.sha256(token.encode("ascii", errors="ignore")).hexdigest()
        now = int(time.time())
        with connect_db() as connection:
            connection.execute("DELETE FROM sessions WHERE expires_at <= ?", (now,))
            row = connection.execute("""
                SELECT users.id, users.name, users.email, users.role
                FROM sessions JOIN users ON users.id = sessions.user_id
                WHERE sessions.token_hash = ? AND sessions.expires_at > ?
            """, (token_hash, now)).fetchone()
        return dict(row) if row else None

    def create_session(self, user_id):
        token = secrets.token_urlsafe(32)
        token_hash = hashlib.sha256(token.encode("ascii")).hexdigest()
        expires_at = int(time.time()) + SESSION_SECONDS
        with connect_db() as connection:
            connection.execute("DELETE FROM sessions WHERE expires_at <= ?", (int(time.time()),))
            connection.execute(
                "INSERT INTO sessions(token_hash, user_id, expires_at) VALUES (?, ?, ?)",
                (token_hash, user_id, expires_at),
            )
        cookie = f"sd_session={token}; Path=/; HttpOnly; SameSite=Strict; Max-Age={SESSION_SECONDS}"
        if self.headers.get("X-Forwarded-Proto", "").lower() == "https":
            cookie += "; Secure"
        return cookie

    def do_GET(self):
        path = urlparse(self.path).path
        if path == "/api/me":
            user = self.current_user()
            if user:
                self.send_json(HTTPStatus.OK, {"user": user})
            else:
                self.send_json(HTTPStatus.UNAUTHORIZED, {"error": "Требуется войти в аккаунт."})
            return
        if path == "/api/content":
            with connect_db() as connection:
                posts = connection.execute("""
                    SELECT posts.id, posts.title, posts.body, posts.media_type,
                           posts.media_url, posts.is_sponsored, posts.created_at,
                           users.name AS author_name
                    FROM posts JOIN users ON users.id = posts.author_id
                    ORDER BY posts.is_sponsored DESC, posts.created_at DESC, posts.id DESC
                """).fetchall()
            self.send_json(HTTPStatus.OK, {"posts": [dict(post) for post in posts]})
            return
        if path not in ("/", "/index.html"):
            self.send_error(HTTPStatus.NOT_FOUND)
            return
        try:
            page = (ROOT / "index.html").read_bytes()
        except OSError:
            self.send_error(HTTPStatus.INTERNAL_SERVER_ERROR)
            return
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(page)))
        self.end_headers()
        self.wfile.write(page)

    def do_POST(self):
        path = urlparse(self.path).path
        if path not in ("/api/register", "/api/login", "/api/logout", "/api/content"):
            self.send_json(HTTPStatus.NOT_FOUND, {"error": "Неизвестный адрес."})
            return
        if not self.origin_is_same_site():
            self.send_json(HTTPStatus.FORBIDDEN, {"error": "Запрос отклонён."})
            return
        data = self.read_json()
        if data is None:
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Не удалось прочитать данные формы."})
            return

        if path == "/api/register":
            self.register(data)
        elif path == "/api/login":
            self.login(data)
        elif path == "/api/logout":
            self.logout()
        else:
            self.create_post(data)

    def do_DELETE(self):
        path = urlparse(self.path).path
        prefix = "/api/content/"
        if not path.startswith(prefix):
            self.send_json(HTTPStatus.NOT_FOUND, {"error": "Публикация не найдена."})
            return
        post_id_text = path[len(prefix):]
        if not post_id_text.isdecimal():
            self.send_json(HTTPStatus.NOT_FOUND, {"error": "Публикация не найдена."})
            return
        if not self.origin_is_same_site():
            self.send_json(HTTPStatus.FORBIDDEN, {"error": "Запрос отклонён."})
            return
        user = self.current_user()
        if not user:
            self.send_json(HTTPStatus.UNAUTHORIZED, {"error": "Требуется войти в аккаунт."})
            return
        if user["role"] != "admin":
            self.send_json(HTTPStatus.FORBIDDEN, {"error": "Недостаточно прав."})
            return
        with connect_db() as connection:
            cursor = connection.execute("DELETE FROM posts WHERE id = ?", (int(post_id_text),))
        if cursor.rowcount == 0:
            self.send_json(HTTPStatus.NOT_FOUND, {"error": "Публикация не найдена."})
            return
        self.send_json(HTTPStatus.OK, {"ok": True})

    def create_post(self, data):
        user = self.current_user()
        if not user:
            self.send_json(HTTPStatus.UNAUTHORIZED, {"error": "Требуется войти в аккаунт."})
            return
        if user["role"] != "admin":
            self.send_json(HTTPStatus.FORBIDDEN, {"error": "Недостаточно прав."})
            return

        title = data.get("title")
        body = data.get("body")
        media_type = data.get("media_type", "none")
        media_url = data.get("media_url", "")
        is_sponsored = data.get("is_sponsored", False)
        if not isinstance(title, str) or not isinstance(body, str):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Укажите заголовок и текст публикации."})
            return
        title = title.strip()
        body = body.strip()
        if not title or len(title) > 160:
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Заголовок должен содержать от 1 до 160 символов."})
            return
        if not body or len(body) > 10_000:
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Текст публикации должен содержать от 1 до 10000 символов."})
            return
        if media_type not in ("none", "image", "video"):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Выберите корректный тип медиа."})
            return
        if not isinstance(media_url, str) or len(media_url) > 2048:
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Ссылка на медиа слишком длинная."})
            return
        media_url = media_url.strip()
        if media_type == "none":
            media_url = ""
        elif not valid_media_url(media_url):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Укажите корректную ссылку на фото или видео (http/https)."})
            return
        if not isinstance(is_sponsored, bool):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Некорректный признак рекламной публикации."})
            return

        with connect_db() as connection:
            cursor = connection.execute("""
                INSERT INTO posts(title, body, media_type, media_url, is_sponsored, author_id, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """, (
                title, body, media_type, media_url, int(is_sponsored),
                user["id"], int(time.time()),
            ))
        self.send_json(HTTPStatus.CREATED, {"id": cursor.lastrowid})

    def register(self, data):
        name = data.get("name")
        email = data.get("email")
        password = data.get("password")
        if not all(isinstance(value, str) for value in (name, email, password)):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Заполните все поля."})
            return
        name = name.strip()
        email = email.strip().lower()
        if not name or len(name) > 80:
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Имя должно содержать от 1 до 80 символов."})
            return
        if len(email) > 254 or not EMAIL_PATTERN.fullmatch(email):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Введите корректный адрес электронной почты."})
            return
        if len(password) < 10 or len(password) > 128:
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Пароль должен содержать от 10 до 128 символов."})
            return

        try:
            with connect_db() as connection:
                cursor = connection.execute(
                    "INSERT INTO users(name, email, password_hash, created_at) VALUES (?, ?, ?, ?)",
                    (name, email, hash_password(password), int(time.time())),
                )
                user_id = cursor.lastrowid
        except sqlite3.IntegrityError:
            self.send_json(HTTPStatus.CONFLICT, {"error": "Аккаунт с этой почтой уже зарегистрирован."})
            return
        user = {"id": user_id, "name": name, "email": email, "role": "user"}
        self.send_json(
            HTTPStatus.CREATED,
            {"user": user},
            (("Set-Cookie", self.create_session(user_id)),),
        )

    def login(self, data):
        email = data.get("email")
        password = data.get("password")
        if not isinstance(email, str) or not isinstance(password, str):
            self.send_json(HTTPStatus.BAD_REQUEST, {"error": "Введите почту и пароль."})
            return
        with connect_db() as connection:
            user = connection.execute(
                "SELECT id, name, email, role, password_hash FROM users WHERE email = ? COLLATE NOCASE",
                (email.strip(),),
            ).fetchone()
        if not user or not verify_password(password, user["password_hash"]):
            self.send_json(HTTPStatus.UNAUTHORIZED, {"error": "Неверная почта или пароль."})
            return
        public_user = {
            "id": user["id"], "name": user["name"], "email": user["email"], "role": user["role"]
        }
        self.send_json(
            HTTPStatus.OK,
            {"user": public_user},
            (("Set-Cookie", self.create_session(user["id"])),),
        )

    def logout(self):
        token = self.session_token()
        if token:
            token_hash = hashlib.sha256(token.encode("ascii", errors="ignore")).hexdigest()
            with connect_db() as connection:
                connection.execute("DELETE FROM sessions WHERE token_hash = ?", (token_hash,))
        self.send_json(
            HTTPStatus.OK,
            {"ok": True},
            (("Set-Cookie", "sd_session=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0"),),
        )

    def log_message(self, format_string, *args):
        # Keep standard request logging without logging form contents or credentials.
        super().log_message(format_string, *args)


def valid_media_url(value):
    if not value or any(character.isspace() for character in value):
        return False
    try:
        parsed = urlparse(value)
    except ValueError:
        return False
    return (
        parsed.scheme in ("http", "https")
        and bool(parsed.hostname)
        and parsed.username is None
        and parsed.password is None
    )


if __name__ == "__main__":
    init_db()
    server = ThreadingHTTPServer((HOST, PORT), SmartDecisionHandler)
    print(f"Smart Decision запущен: http://{HOST}:{PORT}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nСервер остановлен.")
    finally:
        server.server_close()

# Smart Decision

Smart Decision is a Russian-language web application for entertainment recommendations and simple calculations. It includes account registration/login and 17 decision tools.

## Run locally

Requires Python 3.10 or later; there are no third-party packages.

```bash
python3 app.py
```

Open [http://localhost:8765](http://localhost:8765). User accounts and hashed passwords are stored in the local SQLite database `smartdecision.sqlite3`, which is excluded from Git.

Configuration:

- `SMART_DECISION_HOST` — bind address (defaults to `127.0.0.1`)
- `SMART_DECISION_PORT` — HTTP port (defaults to `8765`)
- `SMART_DECISION_DB` — SQLite database path (defaults to `./smartdecision.sqlite3`)

For deployment to the public internet, use a production WSGI/ASGI server behind HTTPS and configure secure cookie handling and operational controls for your hosting environment. The built-in Python server is intended for local development.

## Project files

- `index.html` — responsive web interface and client-side decision tools
- `app.py` — Python HTTP server, registration, login, and session handling
- `src/` — original Java console application

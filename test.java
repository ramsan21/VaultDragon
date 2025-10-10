Awesome—let’s scaffold a clean, deploy-ready Python web app that does:
	•	A simple chatbot endpoint (and a Help screen listing available commands).
	•	A “Check Email / Tracking” flow that opens a dedicated form page (a “window” in the web sense) to enter either an Email ID or a Tracking ID, then shows the result.
	•	Two Kubernetes environments (e.g., staging and prod) with environment-specific properties via ConfigMaps/Secrets.
	•	Correlation ID middleware so every request/response is traceable in logs.
	•	Dockerfile + K8s manifests (with kustomize overlays) so you can build & deploy immediately.

Below is a complete sample project you can copy-paste as a starting point.

⸻

Project layout

chatbot-k8s/
├─ app/
│  ├─ __init__.py
│  ├─ main.py                # FastAPI app entrypoint
│  ├─ config.py              # Reads environment variables
│  ├─ middleware.py          # Correlation-ID + logging
│  ├─ routers/
│  │   ├─ chat.py            # /chat and /help
│  │   └─ tools.py           # /tools/check-email
│  ├─ services/
│  │   └─ email_lookup.py    # mock email / tracking check
│  ├─ templates/
│  │   ├─ base.html
│  │   ├─ index.html
│  │   ├─ help.html
│  │   ├─ check_email_form.html
│  │   └─ check_email_result.html
│  └─ static/
│      └─ styles.css
├─ requirements.txt
├─ Dockerfile
├─ .env.example
├─ k8s/
│  ├─ base/
│  │   ├─ deployment.yaml
│  │   ├─ service.yaml
│  │   ├─ ingress.yaml
│  │   ├─ configmap.yaml
│  │   └─ kustomization.yaml
│  └─ overlays/
│      ├─ staging/
│      │   ├─ kustomization.yaml
│      │   ├─ configmap-patch.yaml
│      │   └─ deployment-patch.yaml
│      └─ prod/
│          ├─ kustomization.yaml
│          ├─ configmap-patch.yaml
│          └─ deployment-patch.yaml
└─ README.md


⸻

Core app code

app/config.py

import os

class Settings:
    APP_NAME: str = os.getenv("APP_NAME", "ChatBot")
    APP_ENV: str = os.getenv("APP_ENV", "staging")  # staging | prod
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")
    # Feature flags / mock toggles
    ENABLE_EMAIL_LOOKUP: str = os.getenv("ENABLE_EMAIL_LOOKUP", "true")
    # If you later integrate real email/tracking systems:
    EMAIL_API_BASE: str = os.getenv("EMAIL_API_BASE", "")
    EMAIL_API_KEY: str = os.getenv("EMAIL_API_KEY", "")
    # Ingress base path (helpful if you mount behind a prefix)
    BASE_PATH: str = os.getenv("BASE_PATH", "/")

settings = Settings()

app/middleware.py

import logging
import uuid
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request

logger = logging.getLogger("uvicorn.access")

class CorrelationIdMiddleware(BaseHTTPMiddleware):
    CORRELATION_HEADER = "X-Correlation-ID"

    async def dispatch(self, request: Request, call_next):
        cid = request.headers.get(self.CORRELATION_HEADER) or str(uuid.uuid4())
        # Attach to state for downstream handlers
        request.state.correlation_id = cid

        # Log inbound
        logger.info(f"[{cid}] IN {request.method} {request.url.path}")

        response = await call_next(request)
        response.headers[self.CORRELATION_HEADER] = cid

        # Log outbound
        logger.info(f"[{cid}] OUT {response.status_code} {request.url.path}")
        return response

app/services/email_lookup.py

from typing import Optional

def lookup(email: Optional[str] = None, tracking_id: Optional[str] = None) -> dict:
    """
    Mock lookup. Replace with real integrations later.
    """
    if email:
        return {
            "type": "email",
            "query": email,
            "status": "OK",
            "found": True,
            "details": {
                "inbox_unread": 2,
                "last_message_from": "notifications@example.com",
            },
        }
    if tracking_id:
        return {
            "type": "tracking",
            "query": tracking_id,
            "status": "OK",
            "found": True,
            "details": {
                "stage": "In Transit",
                "eta": "2 business days",
            },
        }
    return {"status": "ERROR", "message": "Provide email or tracking_id."}

app/routers/chat.py

from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates

templates = Jinja2Templates(directory="app/templates")
router = APIRouter()

COMMANDS = [
    ("/help", "List available commands"),
    ("/chat?msg=hello", "Send a simple chat message"),
    ("/tools/check-email", "Open the Email/Tracking form"),
]

@router.get("/help", response_class=HTMLResponse)
async def help_page(request: Request):
    return templates.TemplateResponse("help.html", {
        "request": request,
        "commands": COMMANDS
    })

@router.get("/chat", response_class=HTMLResponse)
async def chat(request: Request, msg: str = "hi"):
    # Very simple bot (replace with your logic / LLM call)
    reply = f"You said: {msg}. Try /help for commands."
    return templates.TemplateResponse("index.html", {
        "request": request,
        "user_msg": msg,
        "bot_reply": reply
    })

app/routers/tools.py

from fastapi import APIRouter, Form, Request
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from app.config import settings
from app.services.email_lookup import lookup

templates = Jinja2Templates(directory="app/templates")
router = APIRouter(prefix="/tools")

@router.get("/check-email", response_class=HTMLResponse)
async def check_email_form(request: Request):
    return templates.TemplateResponse("check_email_form.html", {
        "request": request,
        "enabled": settings.ENABLE_EMAIL_LOOKUP.lower() == "true"
    })

@router.post("/check-email", response_class=HTMLResponse)
async def check_email_submit(
    request: Request,
    email: str = Form(default=""),
    tracking_id: str = Form(default="")
):
    if not email and not tracking_id:
        # Back to form with message
        return templates.TemplateResponse("check_email_form.html", {
            "request": request,
            "error": "Enter an Email ID or a Tracking ID.",
            "enabled": settings.ENABLE_EMAIL_LOOKUP.lower() == "true"
        })
    result = lookup(email=email or None, tracking_id=tracking_id or None)
    return templates.TemplateResponse("check_email_result.html", {
        "request": request,
        "result": result
    })

app/main.py

import logging
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.middleware import CorrelationIdMiddleware
from app.routers import chat, tools

logging.basicConfig(level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO))

app = FastAPI(title=settings.APP_NAME)

# Static / templates
app.mount("/static", StaticFiles(directory="app/static"), name="static")

# Middlewares
app.add_middleware(CorrelationIdMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], allow_credentials=True,
    allow_methods=["*"], allow_headers=["*"],
)

# Routers
app.include_router(chat.router)
app.include_router(tools.router)

@app.get("/")
def root():
    return {"app": settings.APP_NAME, "env": settings.APP_ENV, "base_path": settings.BASE_PATH}


⸻

Templates (Jinja2)

app/templates/base.html

<!doctype html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>{{ title if title else "ChatBot" }}</title>
  <link rel="stylesheet" href="/static/styles.css">
</head>
<body>
  <header>
    <h1>ChatBot</h1>
    <nav>
      <a href="/help">Help</a>
      <a href="/chat?msg=hello">Chat</a>
      <a href="/tools/check-email">Check Email/Tracking</a>
    </nav>
  </header>
  <main>
    {% block content %}{% endblock %}
  </main>
  <footer>
    <small>Env: {{ env if env else "staging" }}</small>
  </footer>
</body>
</html>

app/templates/index.html

{% extends "base.html" %}
{% block content %}
<h2>Chat</h2>
<form method="get" action="/chat">
  <input name="msg" placeholder="Type a message" value="{{ user_msg or '' }}">
  <button type="submit">Send</button>
</form>

{% if bot_reply %}
  <div class="card"><pre>{{ bot_reply }}</pre></div>
{% endif %}
{% endblock %}

app/templates/help.html

{% extends "base.html" %}
{% block content %}
<h2>Help</h2>
<ul>
  {% for path, desc in commands %}
    <li><code>{{ path }}</code> — {{ desc }}</li>
  {% endfor %}
</ul>
{% endblock %}

app/templates/check_email_form.html

{% extends "base.html" %}
{% block content %}
<h2>Check Email / Tracking</h2>

{% if error %}
  <p class="error">{{ error }}</p>
{% endif %}

{% if not enabled %}
  <p>This feature is disabled in this environment.</p>
{% else %}
<form method="post" action="/tools/check-email">
  <div class="row">
    <label>Email ID</label>
    <input name="email" placeholder="name@example.com">
  </div>
  <div class="row">
    <label>Tracking ID</label>
    <input name="tracking_id" placeholder="ABC123456">
  </div>
  <p class="hint">Enter either Email ID or Tracking ID.</p>
  <button type="submit">Submit</button>
</form>
{% endif %}
{% endblock %}

app/templates/check_email_result.html

{% extends "base.html" %}
{% block content %}
<h2>Lookup Result</h2>
<div class="card">
  <pre>{{ result | tojson(indent=2) }}</pre>
</div>
<a href="/tools/check-email">Run another query</a>
{% endblock %}

app/static/styles.css

body { font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; margin: 0; }
header { background: #222; color: #fff; padding: 12px 16px; }
header h1 { display: inline-block; margin: 0 12px 0 0; font-size: 18px; }
nav a { color: #fff; text-decoration: none; margin-right: 12px; }
main { padding: 16px; }
.card { background: #f7f7f7; border: 1px solid #e0e0e0; padding: 12px; border-radius: 6px; }
.row { margin-bottom: 10px; }
label { display: block; font-weight: 600; margin-bottom: 4px; }
input { width: 320px; padding: 8px; }
button { padding: 8px 12px; cursor: pointer; }
.error { color: #b00020; }
.hint { color: #666; font-size: 12px; }


⸻

Dependencies & Docker

requirements.txt

fastapi==0.115.5
uvicorn[standard]==0.30.6
jinja2==3.1.4

Dockerfile

FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app

EXPOSE 8080
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]

.env.example

APP_NAME=ChatBot
APP_ENV=staging
LOG_LEVEL=INFO
ENABLE_EMAIL_LOOKUP=true
BASE_PATH=/
EMAIL_API_BASE=
EMAIL_API_KEY=


⸻

Kubernetes (with kustomize overlays)

This lets you keep common “base” manifests and override only what changes per environment.

k8s/base/configmap.yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: chatbot-config
data:
  APP_NAME: "ChatBot"
  APP_ENV: "staging"
  LOG_LEVEL: "INFO"
  ENABLE_EMAIL_LOOKUP: "true"
  BASE_PATH: "/"

k8s/base/deployment.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: chatbot
spec:
  replicas: 2
  selector:
    matchLabels:
      app: chatbot
  template:
    metadata:
      labels:
        app: chatbot
    spec:
      containers:
        - name: chatbot
          image: your-dockerhub-username/chatbot-k8s:latest
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: chatbot-config
          readinessProbe:
            httpGet:
              path: /help
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 20

k8s/base/service.yaml

apiVersion: v1
kind: Service
metadata:
  name: chatbot-svc
spec:
  selector:
    app: chatbot
  ports:
    - port: 80
      targetPort: 8080
      protocol: TCP

k8s/base/ingress.yaml

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: chatbot-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
    - host: chatbot.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: chatbot-svc
                port:
                  number: 80

k8s/base/kustomization.yaml

resources:
  - configmap.yaml
  - deployment.yaml
  - service.yaml
  - ingress.yaml

Staging overlay

k8s/overlays/staging/configmap-patch.yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: chatbot-config
data:
  APP_ENV: "staging"
  LOG_LEVEL: "DEBUG"
  ENABLE_EMAIL_LOOKUP: "true"

k8s/overlays/staging/deployment-patch.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: chatbot
spec:
  replicas: 1
  template:
    spec:
      containers:
        - name: chatbot
          image: your-dockerhub-username/chatbot-k8s:staging

k8s/overlays/staging/kustomization.yaml

resources:
  - ../../base
patchesStrategicMerge:
  - configmap-patch.yaml
  - deployment-patch.yaml
nameSuffix: -stg

Prod overlay

k8s/overlays/prod/configmap-patch.yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: chatbot-config
data:
  APP_ENV: "prod"
  LOG_LEVEL: "INFO"
  ENABLE_EMAIL_LOOKUP: "true"

k8s/overlays/prod/deployment-patch.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: chatbot
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: chatbot
          image: your-dockerhub-username/chatbot-k8s:prod

k8s/overlays/prod/kustomization.yaml

resources:
  - ../../base
patchesStrategicMerge:
  - configmap-patch.yaml
  - deployment-patch.yaml
nameSuffix: -prod

This gives you two distinct environments with small diffs (replicas, log level, image tags, etc.).
You can also set different Ingress hosts per env by adding an ingress patch in each overlay if needed.

⸻

How to run locally

# 1) Create venv & install
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# 2) Run
uvicorn app.main:app --reload --port 8080

# Open: http://localhost:8080/help


⸻

Build & push image

docker build -t your-dockerhub-username/chatbot-k8s:staging .
docker push your-dockerhub-username/chatbot-k8s:staging

# For prod
docker build -t your-dockerhub-username/chatbot-k8s:prod .
docker push your-dockerhub-username/chatbot-k8s:prod


⸻

Deploy to Kubernetes

# Staging
kubectl apply -k k8s/overlays/staging

# Prod
kubectl apply -k k8s/overlays/prod

(Optionally, use separate namespaces: add namespace: staging / namespace: prod in each overlay’s kustomization.yaml and create those namespaces.)

⸻

Environment properties strategy
	•	ConfigMap for non-secret, environment-specific values (e.g., APP_ENV, LOG_LEVEL, feature flags, base path, API base URLs).
	•	Secret for credentials (e.g., EMAIL_API_KEY)—create a Secret per env and mount via envFrom.
	•	Overlays (kustomize) to vary replicas, image tags, ingress hostnames, and any per-env toggles.

Example Secret you might add later:

apiVersion: v1
kind: Secret
metadata:
  name: chatbot-secrets
type: Opaque
stringData:
  EMAIL_API_KEY: "replace-me"

Then mount in Deployment:

envFrom:
  - configMapRef:
      name: chatbot-config
  - secretRef:
      name: chatbot-secrets


⸻

Extending the functionality
	•	Chatbot logic: replace the trivial echo in chat.py with your LLM gateway or rules.
	•	Email/Tracking: swap the mock email_lookup.py with real integrations (Gmail API, ServiceNow/Jira ticket lookup, courier API, etc.).
	•	Correlation ID propagation: forward X-Correlation-ID to downstream services when you call them.
	•	Filtering noisy routes: if you add health checks and don’t want them logged, add a small allow/deny list in CorrelationIdMiddleware.

⸻

If you want, I can package all these files into a downloadable zip next—just say “make the zip,” and I’ll generate it for you.
That changes things: the Copilot SDK runs on top of the Copilot CLI, so it won't work for you. LiteLLM (or calling the Copilot API directly from your own script) would technically work, but it imitates VS Code. If your bank blocked the CLI, that route will likely be seen as bypassing the block, so I'd avoid it.

## What still works with your license

**Option A (recommended): use Copilot Chat in VS Code as the agent and chat**

Copilot Chat **Agent mode** already combines agent and chat, and it isn't blocked. You don't need your own LLM layer at all:
- Your **pods** keep the backend API (no LLM).
- On the desktop, you build a **Python CLI toolbox**, e.g. `python ops.py kibana-search ...`, which calls the pods.
- A `.github/copilot-instructions.md` file teaches Copilot to run those commands in the terminal, read the output and investigate.

This needs no CLI, no SDK and no MCP, so it's the least likely to hit corporate policy.

**Option B: a small VS Code extension using the official Language Model API**

VS Code's `vscode.lm` API lets an extension use your Copilot models officially. You'd add an `@ops` chat participant whose tools call your pods. The trade-offs are that it's TypeScript, not Python, and your company may block installing custom extensions (VSIX).

**Option C: a company-approved LLM**

If the bank provides Azure OpenAI or similar, your Python app plus LangGraph works as originally planned. Only the model connection changes.

## Prompt for Option A

Paste this into Copilot Chat in Agent mode, with your project open:

```
Copilot CLI and the Copilot SDK are blocked in my company, so do NOT use them, and do NOT
call Copilot/GitHub model APIs from Python. Copilot Chat (Agent mode in VS Code) will be the
AI agent. My Python code will only be a toolbox that Copilot runs in the terminal.

First analyse my existing codebase (desktop part and the backend running in Kubernetes pods)
and summarise what exists, what works and what is missing. Propose a plan and wait for my
approval before changing code.

BUILD THIS

1. Backend in Kubernetes pods (no LLM). Reuse my existing code.
   REST endpoints: DB read-only query (SELECT/WITH only, checked with sqlglot, row limit,
   timeout), list/describe tables, Kibana search (always time-bounded), Grafana dashboards /
   panel query / firing alerts, ADO work items / WIQL search / pipeline runs and logs / PRs,
   /health and /ready. Credentials from Kubernetes Secrets. Mask secrets/PII in responses.
   Audit log of every request.

2. Desktop CLI toolbox: ops.py (use Typer), one sub-command per backend endpoint, plus local
   repo commands (search code, read file, git log, git diff) restricted to REPO_PATH.
   - Calls the backend over HTTPS with a bearer token read from the OS keyring.
   - Supports corporate proxy and custom CA bundle from config.
   - Output is compact, readable text by default, with a --json flag. Truncate long output
     and say it was truncated.
   - Clear error messages (e.g. "Kibana timed out, use a smaller time range").
   - Read-only by default. Any command that creates/updates something in ADO must print what
     it will do and require a --confirm flag.
   - `python ops.py --help` and each sub-command's --help must explain arguments with examples,
     because Copilot will read them to decide what to run.

3. .github/copilot-instructions.md that tells Copilot Agent mode:
   - "You are an L2 production support assistant. To investigate, run the ops.py commands in
     the terminal. Run `python ops.py --help` first if unsure of a command."
   - Investigation order: clarify time range and affected user/service, then Kibana logs,
     Grafana metrics/alerts, DB data, related code in the repo, related ADO items/pipelines.
   - Always show the commands run and the evidence found, then a root-cause summary and
     suggested next steps.
   - Never use --confirm (write actions) without asking me first.
   - Never print or echo secrets.

4. .github/prompts/ folder with reusable prompt files:
   investigate-incident.prompt.md, check-pipeline-failure.prompt.md,
   find-error-in-code.prompt.md.

5. Tests: pytest for the backend (mocked DB and HTTP, SQL guard rejects writes/DDL/multiple
   statements) and for ops.py (backend mocked).

WORK IN STEPS and stop after each one so I can test:
Step 0: analysis + plan. Step 1: backend endpoints. Step 2: ops.py toolbox.
Step 3: copilot-instructions.md and prompt files. Step 4: tests. Step 5: README.
List the files you changed in each step and do not rewrite working code without asking.
```

After it's built, you'd use it by asking in Agent mode something like: *"Login failures for user X since 2pm, investigate."* Copilot will run your `ops.py` commands, read the results and summarise.

One thing to check first: in Agent mode, VS Code asks permission before running terminal commands. Keep that on, or allow only `python ops.py` commands. Don't auto-approve everything.

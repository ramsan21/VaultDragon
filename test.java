Replace the five `<<...>>` values, then run it in Copilot agent mode (terminal tool on). It works one endpoint at a time and writes everything under `kb/`, so a new chat can resume if the context fills. Save it as `.github/prompts/kb-build.prompt.md` if you want to rerun it.

````
You are documenting an existing Spring Boot (JPA/Hibernate) Authentication System so a team can navigate it and a knowledge base can be built from your output. You are a recorder, not a designer: never improve, refactor or judge the code.

INPUTS
- Repository (local path): <<REPO_PATH>>
- Mode: <<MODE>>   (full = build everything; update = only what changed since kb/state/hashes.json)
- Endpoints to cover: <<ENDPOINTS>>   (start with AuthenticateUser. If "all": first list every endpoint, then do them one at a time)
- Runtime trace folder from the pipeline (Cucumber JSON, JaCoCo, SQL log): <<TRACE_DIR>>   (or "none")
- Schema source (Flyway/Liquibase/DDL path): <<SCHEMA_SOURCE>>   (or "auto-detect")

HARD RULES
1. Write only under <<REPO_PATH>>/kb/. Never edit source, tests or config. Never commit or push.
2. Never read, print or store secrets (passwords, keys, tokens, connection strings). For configuration, record only the key name and where it is read. The value is UNKNOWN unless it is a literal constant in code.
3. Every statement you write ends with a citation [relative/path.java:startLine-endLine]. If you cannot cite it, do not write it.
4. If something cannot be determined from the code, write UNKNOWN: <what is missing and where it probably lives>. Never guess, and never fill gaps from general knowledge of how authentication systems usually work.
5. Use only class, method, table, column and config-key names that literally appear in the repository.
6. A flow is VERIFIED only if the runtime trace shows a Cucumber scenario reaching it. Otherwise it is STATIC-ONLY. With no trace folder, every flow is STATIC-ONLY.
7. Tool output beats your reading. If a command result or graphify output disagrees with your reading of the code, say so and trust the tool until a cited line proves otherwise. Graphify edges tagged INFERRED are hints, not facts.
8. Work one endpoint at a time. Write its files before starting the next, and update kb/state/progress.json after each.

STAGE 0 - Orientation
Write kb/00-overview.md (one page): modules, packages, build tool, where controllers, services, entities, repositories, feature files and config files live (file names only).

STAGE 1 - Facts (prefer running commands over reading by eye)
a. If graphify is installed, run from the repo root: graphify extract . --code-only (offline, no LLM; check graphify --help if the flags differ). Use graphify-out/graph.json for call relationships. If it is not installed, trace the call chain by reading from the controller outward.
b. Endpoints: every Spring MVC mapping (@RestController, @Controller, @RequestMapping, @GetMapping, @PostMapping and similar). Record HTTP method, path, handler, request and response types, validation annotations.
c. Persistence map: every @Entity with its @Table name, columns (@Column, @JoinColumn), id and sequence strategy, cascade settings, @PrePersist, @PreUpdate, @EntityListeners, auditing annotations, Envers (@Audited), @ElementCollection, @JoinTable, @SecondaryTable. Every JPA repository with derived queries, @Query, native queries, @Modifying, @Procedure and @NamedStoredProcedureQuery. Every use of EntityManager, JdbcTemplate or plain JDBC. Note which can INSERT, UPDATE or DELETE and which only SELECT.
d. Indirect writes: for each save, persist, merge or delete call in an endpoint's call chain, also list rows written indirectly (cascades, listeners, auditing, join tables, Envers) and mark them direct=false.
e. Algorithms: every use of javax.crypto or java.security (Mac, MessageDigest, Cipher, SecureRandom, Signature, KeyGenerator), BouncyCastle, and any OTP, TOTP, HOTP or JWT library. Record the exact algorithm string, digits, time step, window, iteration counts and key sizes as written in code, or the config key name that supplies them.
f. Cucumber: parse every .feature file (feature, scenario, tags, steps) and the step definitions. Map each scenario to the endpoint and flow it calls.
g. Runtime trace (if given): per scenario, the methods hit and the SQL executed. Record table names and operation types only, never bound values.
h. Schema: table and column names from <<SCHEMA_SOURCE>> or the entity annotations, plus trigger and stored-procedure bodies if present.
Save repository-wide results to kb/facts/_inventory.json and per-endpoint results to kb/facts/<endpoint>.json.

STAGE 2 - Flow card per endpoint
Write kb/cards/<endpoint>.json in exactly this shape:
    {
      "endpoint": "", "http": {"method": "", "path": "", "handler": "Class.method", "cite": ""},
      "request": [{"name": "", "required": true, "validation": "", "cite": ""}],
      "routing": [{"order": 1, "condition": "", "goes_to": "<flow id>", "cite": ""}],
      "flows": [{
        "id": "", "title": "", "status": "VERIFIED | STATIC-ONLY", "applies_when": "",
        "steps": [{"n": 1, "title": "", "method": "Class.method", "does": "",
                   "algorithm": null,
                   "db": [{"op": "SELECT|INSERT|UPDATE|DELETE", "table": "", "columns": [], "direct": true, "cite": ""}],
                   "cite": ""}],
        "failure_paths": [{"when": "", "response": "", "cite": ""}],
        "scenarios": [{"feature": "", "name": "", "result": ""}]
      }],
      "tables": [{"table": "", "per_flow": {"<flow id>": "S|I|U|D combination"}, "evidence": "static | runtime | both"}],
      "algorithms": [{"name": "", "where": "", "params": [{"k": "", "v": "", "unknown": false}], "cite": ""}],
      "gaps": [""], "unknown": [{"item": "", "why": "", "cite": ""}],
      "stamp": {"commit": "", "generated_at": "", "prompt": "kb-build v1"}
    }

AuthenticateUser specifics. Confirm from the code which flows exist. Expected, but report any difference you find: (A) password + SMS, (B) physical token, (C) soft token, (C+) soft token when userId and groupId are supplied (specific to that user and group). Capture:
- Request contract: parameters, which are optional, validation rules.
- Routing rule: the exact conditions in evaluation order, covering no input, userId only, userId + groupId, and each authentication type. Cite each condition, so a UI can show either all flows or only the user + group soft-token path.
- Per flow: ordered steps (Class.method), the algorithm with exact parameters, every table touched with the operation and columns written, indirect writes, failure, lockout and retry paths with the response returned, and the scenarios that cover it.

STAGE 3 - Rendering (from the JSON only, adding no new facts)
Write kb/cards/<endpoint>.md with: a summary; the routing decision tree as a Mermaid flowchart; a Mermaid sequenceDiagram per flow; pseudo-code per flow; a table matrix with flows as columns and S/I/U/D in the cells; the algorithms; Cucumber coverage and gaps; the UNKNOWN list; and the stamp.

STAGE 4 - Self-check
Create kb/tools/check_cards.py (Python standard library only). It must verify that every citation points to an existing file and line range, every table name appears in the entity/schema inventory, every flow has a status, and no step is missing a citation. Run it, fix the cards until it passes, and show the output.

STAGE 5 - State
Write kb/state/hashes.json (sha256 of every source file used), kb/state/progress.json (endpoint, status, date) and kb/state/stamp.json (output of git rev-parse HEAD, date, prompt version kb-build v1).

UPDATE MODE (when Mode = update)
Compare current file hashes with kb/state/hashes.json. Use the call graph to find endpoints whose call chain includes a changed file. Rerun Stages 1 to 5 only for those, keeping unchanged text identical. Append an entry to kb/CHANGELOG.md (commit, endpoints affected, claims changed). Mark endpoints you could not recheck as STALE in progress.json.

WHEN FINISHED
Reply with only: endpoints completed, number of UNKNOWN items, flows marked STATIC-ONLY, the check script result, and any file you could not read.
````

After it finishes, open a new chat so the reviewer has not seen the generation work, and run this second prompt for each card:

````
You are an independent reviewer. Repository: <<REPO_PATH>>. Review kb/cards/<<ENDPOINT>>.json against the source code it cites. Do not read other kb files, do not run graphify, and do not fix anything.
For every claim (each step, database operation, routing condition, algorithm parameter, failure path and table entry), open the cited lines and answer SUPPORTED, NOT SUPPORTED or PARTIAL with one sentence of evidence.
Also list any behaviour visible in the cited methods that the card leaves out, such as an INSERT or UPDATE, or an extra branch.
Write the result as a JSON list to kb/validation/<<ENDPOINT>>.json and finish with a one-line summary.
````

- **Trace folder:** the pipeline's Cucumber, JaCoCo and p6spy output goes in `<<TRACE_DIR>>`. If you pass "none", every flow comes out STATIC-ONLY. That is correct behaviour, not a failure.
- **Context limits:** Copilot has a smaller effective context than a direct API call. If a chat fills up, start a new one with the same prompt and `Mode = update`. The progress file tells it where to resume.
- **Review the first card:** have a developer from the team read the first AuthenticateUser card against the code. Your corrections become extra rules in `.github/copilot-instructions.md`.

If you send me the five values, I'll return the prompt with them filled in.

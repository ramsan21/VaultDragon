**Best option: a graph as the main store, with a vector index only for the search box.**
- Your UI looks things up by exact keys: first the endpoint, then userId + groupId. A vector DB finds similar text, not the exact path, so it can't be the source of truth.
- The structure I'd use:
  - JSON cards in Git are the source of truth.
  - Neo4j holds the navigation graph (endpoint → flow → step → table).
  - A small pgvector or Qdrant index serves the free-text search box.
- If you build only one thing first, build the graph.

**What I took "insert should become select" to mean:**
- **Lineage:** wherever a flow inserts or updates a table, the KB links it to every place that later selects from that table.
- **Read-only checks:** each write also gets a stored SELECT that would show the written row. These are never run, and Copilot never touches a database.
- If you meant something else, tell me and I'll change that part.

In v2 the cards also carry data-driven view rules, so the UI shows all flows by default and only the user + group journey when both fields are given. This is the full prompt; run it in Copilot agent mode. Replace the five `<<...>>` values.

````
You are documenting an existing Spring Boot (JPA/Hibernate) Authentication System so a team can navigate it in a UI and so a graph and search index can be built from your output. You are a recorder, not a designer: never improve, refactor or judge the code.

INPUTS
- Repository (local path): <<REPO_PATH>>
- Mode: <<MODE>>   (full = build everything; update = only what changed since kb/state/hashes.json)
- Endpoints: <<ENDPOINTS>>   (use "all" for every endpoint; start with AuthenticateUser)
- Runtime trace folder from the pipeline (Cucumber JSON, JaCoCo, SQL log): <<TRACE_DIR>>   (or "none")
- Schema source (Flyway/Liquibase/DDL path): <<SCHEMA_SOURCE>>   (or "auto-detect")

HARD RULES
1. Write only under <<REPO_PATH>>/kb/. Never edit source, tests or config. Never commit or push.
2. Never connect to a database and never run any SQL. Database knowledge comes from code and schema files only.
3. Never read, print or store secrets (passwords, keys, tokens, connection strings, bound SQL values). For configuration record only the key name and where it is read. The value is UNKNOWN unless it is a literal constant in code.
4. Every statement ends with a citation [relative/path.java:startLine-endLine]. If you cannot cite it, do not write it.
5. If something cannot be determined from the code, write UNKNOWN: <what is missing and where it probably lives>. Never guess, and never fill gaps from general knowledge of how authentication systems usually work.
6. Use only class, method, table, column and config-key names that literally appear in the repository. Copy names exactly.
7. A flow is VERIFIED only if the runtime trace shows a Cucumber scenario reaching it. Otherwise it is STATIC-ONLY. With no trace folder, every flow is STATIC-ONLY.
8. Tool output beats your reading. If a command result or graphify output disagrees with your reading of the code, say so and trust the tool until a cited line proves otherwise. Never carry a graphify INFERRED edge into any output.
9. Work one endpoint at a time. Write its files before starting the next, and update kb/state/progress.json after each.

STAGE 0 - Orientation
Write kb/00-overview.md (one page): modules, packages, build tool, where controllers, services, entities, repositories, feature files and config files live (file names only).

STAGE 1 - Facts (run commands; save raw output to kb/facts/raw/ so a reviewer can compare)
a. If graphify is installed, run from the repo root: graphify extract . --code-only (offline, no LLM; check graphify --help if flags differ). Use graphify-out/graph.json for call relationships. If it is not installed, trace call chains by reading from the controller outward.
b. Endpoints: every Spring MVC mapping (@RestController, @Controller, @RequestMapping, @GetMapping, @PostMapping and similar). Record HTTP method, path, handler, request and response types, validation annotations. Completeness check: the number of mapping annotations found by grep must equal the endpoints in the inventory. Record any difference in progress.json.
c. Persistence: every @Entity with its @Table name, columns (@Column, @JoinColumn), id and sequence strategy, cascade settings, @PrePersist, @PreUpdate, @EntityListeners, auditing annotations, Envers (@Audited), @ElementCollection, @JoinTable, @SecondaryTable. Every JPA repository with derived queries, @Query, native queries, @Modifying, @Procedure and @NamedStoredProcedureQuery. Every use of EntityManager, JdbcTemplate or plain JDBC.
d. For each save, persist, merge or delete in an endpoint's call chain, also list rows written indirectly (cascades, listeners, auditing, join tables, Envers) with direct=false.
e. Algorithms: every use of javax.crypto or java.security (Mac, MessageDigest, Cipher, SecureRandom, Signature, KeyGenerator), BouncyCastle, and any OTP, TOTP, HOTP or JWT library. Record the exact algorithm string, digits, time step, window, iteration counts and key sizes as written, or the config key name that supplies them.
f. Cucumber: parse every .feature file (feature, scenario, tags, steps) and the step definitions. Map each scenario to the endpoint and flow it calls.
g. Runtime trace (if given): per scenario, the methods hit and SQL executed. Record table names and operation types only.
h. Schema: table and column names from <<SCHEMA_SOURCE>> or the entity annotations, plus trigger and stored-procedure bodies if present.
i. Request branching: for every endpoint, which request fields (for example userId, groupId, authentication type) change the path the code takes, and the exact branching line.
Save repository-wide results to kb/facts/_inventory.json and per-endpoint results to kb/facts/<endpoint>.json.

STAGE 2 - Flow card per endpoint
Write kb/cards/<endpoint>.json in exactly this shape:
    {
      "endpoint": "", "http": {"method": "", "path": "", "handler": "Class.method", "cite": ""},
      "request": [{"name": "", "required": true, "validation": "", "cite": ""}],
      "routing": [{"order": 1, "condition": "", "goes_to": "<flow id>", "cite": ""}],
      "views": {
        "default": {"show_flows": ["all"]},
        "by_input": [{"when_present": ["userId", "groupId"], "when_absent": [], "show_flows": ["<flow id>"], "cite": ""}]
      },
      "flows": [{
        "id": "", "title": "", "status": "VERIFIED | STATIC-ONLY", "applies_when": "",
        "steps": [{"n": 1, "title": "", "method": "Class.method", "does": "",
                   "algorithm": null,
                   "db": [{"op": "SELECT|INSERT|UPDATE|DELETE", "table": "", "columns": [], "direct": true,
                           "verify_select": "", "cite": ""}],
                   "cite": ""}],
        "failure_paths": [{"when": "", "response": "", "cite": ""}],
        "scenarios": [{"feature": "", "name": "", "result": ""}]
      }],
      "tables": [{"table": "", "per_flow": {"<flow id>": "S|I|U|D combination"}, "evidence": "static | runtime | both"}],
      "algorithms": [{"name": "", "where": "", "params": [{"k": "", "v": "", "unknown": false}], "cite": ""}],
      "gaps": [""], "unknown": [{"item": "", "why": "", "cite": ""}],
      "stamp": {"commit": "", "generated_at": "", "prompt": "kb-build v2"}
    }

Rules for the new fields:
- views.by_input: one entry for each distinct combination of request fields the code branches on (from Stage 1i), each citing the branching line. If the code does not branch on a field, do not invent an entry. The UI shows default when no input is given and the matching by_input entry when those fields are supplied.
- verify_select: for every INSERT, UPDATE or DELETE, write the SELECT that would show the affected row, in the form SELECT <written columns, or * if unknown> FROM <table> WHERE <key column> = :placeholder. Key columns come only from the entity id, the repository method name, or the conditions in the @Query or native SQL. If the key cannot be determined, write UNKNOWN. These are stored only and never executed. For SELECT operations leave it empty.

AuthenticateUser specifics. Confirm from the code which flows exist. Expected, but report any difference: (A) password + SMS, (B) physical token, (C) soft token, (C+) soft token when userId and groupId are supplied (specific to that user and group). Capture the request contract, the routing conditions in evaluation order (no input, userId only, userId + groupId, each authentication type), and per flow: ordered steps, exact algorithm parameters, every table touched with operation and columns written, indirect writes, failure, lockout and retry paths with the response returned, and covering scenarios.

STAGE 3 - Rendering (from the JSON only, adding no new facts)
Write kb/cards/<endpoint>.md with: a summary; the routing decision tree as a Mermaid flowchart; a Mermaid sequenceDiagram per flow; pseudo-code per flow; a table matrix (flows as columns, S/I/U/D in cells); algorithms; Cucumber coverage and gaps; the UNKNOWN list; and the stamp.

STAGE 4 - Exports for the UI, graph and search (after the cards, built only from the cards)
a. kb/export/index.json: one entry per endpoint with id, method, path, title, card file, list of input combinations from views.by_input, and counts of VERIFIED flows, STATIC-ONLY flows and UNKNOWN items.
b. kb/export/graph.json with nodes {id, type, label, props} and edges {from, to, type, evidence, cite}.
   Node types: Endpoint, Flow, Step, Method, Table, Algorithm, Scenario.
   Edge types: ROUTES_TO, HAS_STEP, CALLS, WRITES, READS, USES_ALGORITHM, COVERED_BY, FEEDS.
   Rules: evidence is EXTRACTED (read directly from a cited line), RUNTIME (seen in the trace) or STATIC. Use only edges proven by a cited line or a tool result. FEEDS goes from a Step that INSERTs or UPDATEs table T to any Step, in any flow or endpoint, that SELECTs T, with props.table = T. Build FEEDS only from the db entries, never from naming similarity.
c. kb/export/lineage.json: for each table, the steps that write it and the steps that read it, across all endpoints, plus its verify_select statements.
d. kb/export/chunks.jsonl: one JSON object per line for each routing rule, step, table entry, algorithm, failure path and gap, with fields id, endpoint, flow, kind, text, cite, evidence, commit. The text is made only of the card's own sentences. Never copy source code or secret values.

STAGE 5 - Self-check
Create kb/tools/check_cards.py (Python standard library only). It must verify that: every citation points to an existing file and line range; every table and column name appears in the entity or schema inventory; every flow has a status; no step lacks a citation; every views.by_input entry points to an existing flow; every verify_select uses only known table and column names or is UNKNOWN and contains no INSERT, UPDATE or DELETE; every graph edge references existing nodes; every FEEDS edge matches a write and a read in the db entries. Run it, fix the files until it passes, and show the output.

STAGE 6 - State
Write kb/state/hashes.json (sha256 of every source file used), kb/state/progress.json (endpoint, status, date, completeness check result) and kb/state/stamp.json (git rev-parse HEAD, date, prompt version kb-build v2).

UPDATE MODE (when Mode = update)
Compare current file hashes with kb/state/hashes.json. Use the call graph to find endpoints whose call chain includes a changed file. Rerun Stages 1 to 6 only for those, keeping unchanged text identical, then regenerate the exports. Append an entry to kb/CHANGELOG.md (commit, endpoints affected, claims changed). Mark endpoints you could not recheck as STALE in progress.json.

WHEN FINISHED
Reply with only: endpoints completed, number of UNKNOWN items, flows marked STATIC-ONLY, any completeness mismatch, the check script result, and any file you could not read.
````

- **Review step:** run the independent review prompt from before in a fresh chat for each card. Add this line to it: "Also check every verify_select, every views.by_input entry, and that every INSERT or UPDATE in the cited methods appears in the card's db list."
- **Using the exports:**
  - The UI reads `kb/export/index.json` and the cards.
  - Neo4j is loaded from `graph.json`.
  - The embedding job reads `chunks.jsonl`. Use a self-hosted embedding model so code stays inside your network.
- **Large codebases:** if "all" is too much for one Copilot chat, run it with a few endpoints at a time. The progress file keeps the runs consistent.

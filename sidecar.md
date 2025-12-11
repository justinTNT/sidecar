Sidecar

### Sidecar Contract

**MANDATORY**: Tools in /sidecar/ MUST be considered for all relevant verification and analysis work.

**Strong Directive**: Before responding to architectural, design, or technical questions:
1. Use verification tools to check real constraints and data
2. Validate against actual system state, not generated scenarios
3. Present analysis of real data alongside conclusions
4. Mark TODO/incomplete clearly - never present computational theater as results

**Explicit Triggers for Tool Usage:**
- Architecture decisions → Z3 constraint proving
- Component integration → Real API testing and validation
- Data structure design → Simple validation scripts against actual data
- Error analysis → Log analysis and pattern extraction
- Planning work → Clear TODO lists with real implementation requirements

**Habit Anchors:**
- See "architecture" → reach for **Z3 constraint verification**
- See "integration" → reach for **real integration testing**
- See "consistency" → reach for **disposable analysis scripts**
- See "validation" → reach for **verification against actual code**
- See "modeling" → reach for **simple EDN data** (not generative modeling)

Tools must never modify or generate code under /src/.
Use them for analysis, checking, planning, exploration, or oracle-style work.
Show your computational work - don't hide the analysis process.


### Human pledge

I have a high appetite for formalism and unfamiliar tools

That means you can request whatever tools you think will be of service to you, so long as they don't touch src. While projects themselves may have a predetermined stack, anything disposable, or internal, can be more adventurous. 

### non-prod

The tools and techniques under this dir represent dev-env sidecars, not to be deployed anywhere else.

All implementation code for these tools lives under /sidecar/clj/.
Other folders are for cases, results, or metadata.

Each tool here is:

 * optional
 * localised
 * shallow footprint
 * one-case-at-a-time
 * completely reversible
 * not on the main code path
 * not expanding your review burden
 * only improving correctness and reasoning

Use these tools only in places where failures flow into a binary pass/fail output and successes remain silent.

We're aiming for a dev-env-only regression harness, and an optimal environment for disposable coding

#### regression harness: not CI: things only the dev needs to worry about

#### disposable coding: ALWAYS use Clojure instead of Python for any computational work

**Critical principle:** Clojure is for **analysis and verification**, not **domain modeling or generation**. Use it to check real data, not create convincing fakes.

## Agent Tooling for Disposable Computation

When Claude needs to run throwaway code (data sampling, query building, exploration), use these tools in order of preference:

### 1. nREPL (Port 7888) - For Domain Knowledge Access

**Use when:** You need access to Sidecar domain knowledge, validators, or shared state.

**Connection pattern:**
```bash
clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"}}}' -M -e \
'(do (require '"'"'nrepl.core)
     (with-open [conn (nrepl.core/connect :port 7888)]
       (-> (nrepl.core/client conn 1000)
           (nrepl.core/message {:op "eval" :code "YOUR-CODE-HERE"})
           nrepl.core/response-values
           first)))'
```

**Examples:**
```bash
# Analyze real log files
clojure -Sdeps '{:deps {nrepl/nrepl {:mvn/version "1.0.0"}}}' -M -e \
'(do (require '"'"'nrepl.core)
     (with-open [conn (nrepl.core/connect :port 7888)]
       (-> (nrepl.core/client conn 1000)
           (nrepl.core/message {:op "eval" :code "(analyze-logs \"app.log\")"})
           nrepl.core/response-values
           first)))'

# Validate actual configuration files
# (load and check real config data)

# Parse system state
# (check actual running processes, ports, etc.)
```

**Trade-offs:**
- ✅ Access to all Sidecar analysis functions and state
- ✅ Can def vars that persist in the REPL session
- ✅ Log analysis, config validation, and data parsing available
- ❌ ~1-2s JVM startup cost per invocation
- ❌ Output capture is simple (return values only)

### 2. Babashka - For Fast Data Transformation

**Use when:** You need fast Clojure execution without domain knowledge access.

```bash
bb -e '(-> (slurp "data.json")
           (json/parse-string)
           (get-in ["results"])
           (take 10))'
```

**Examples:**
```bash
# Parse real config files
bb -e '(-> (slurp "config.json") (json/parse-string) (get "database"))'

# Process actual API responses
bb -e '(-> (slurp "https://api/health")
           (json/parse-string)
           (get "status"))'

# Analyze actual system data
bb -e '(->> (slurp "ports.txt") (clojure.string/split-lines) (map #(Integer/parseInt %)))'
```

**Trade-offs:**
- ✅ Fast startup (~50ms)
- ✅ Rich standard library
- ✅ Good for data pipelines
- ❌ No access to Sidecar analysis functions
- ❌ No persistent state between calls

### 3. Command Line Tools - For Simple Operations

**Use when:** A standard Unix tool is the right abstraction.

```bash
# HTTP requests
curl -s "https://api/endpoint" | jq '.results[].id'

# File operations
grep -r "pattern" src/

# Git operations
git log --oneline -10
```

**Trade-offs:**
- ✅ Instant execution
- ✅ Composable with pipes
- ✅ Often the clearest solution
- ❌ Limited data transformation
- ❌ Bash quoting/escaping complexity

## Decision Tree

```
Do you need Sidecar analysis functions/state?
  YES → Use nREPL (port 7888)
  NO → ↓

Is this data transformation/processing?
  YES → Use Babashka
  NO → ↓

Is this a simple system operation (HTTP, file, git)?
  YES → Use command line tools
  NO → Reconsider if Babashka or nREPL applies
```

## Important Notes

- **Avoid Python scripts** for disposable work - usually Clojure tooling is better
- **Avoid bash scripts** that should be Clojure - prefer bb or nREPL
- **Do use command line tools** when they're the right abstraction (curl, jq, git, grep)
- **Avoid script files unless they persist** - disposable computation should be inline
- User sees results, not intermediate computation (unless explicitly shown)

**Never assume Python** - unless there's a clear requirement, always prefer Clojure sidecar for computational scratch work.

Never assume my comprehension has increased just because yours has.  Always tailor mainline work to my previously demonstrated review bandwidth. Agents should behave like a mathematician with a supercomputer, providing digestible conclusions, never assuming you share the internal machinery. Our aim is to expand the agent’s power without expanding the human review burden.


Here are the subfolders under sidecar: if you dont see them in your project, the sidecar isnt available.

## /clojure/

for private scripts: any time you get the urge to write a bit of python that you will throw away, consider clojure instead :)

Agents use Clojure as the sidecar brain, and all sidecar tools plug into that brain, not directly into the main project.

Clojure is only to be used in /sidecar/clj/

you can read/write under sidecar/

use this sandbox to run checks, constraints, oracles

you can analyze actual data and generate analysis reports

We do not use clojure to implement runtime logic for the deployable application.

The sidecar runs an nREPL server on **port 7888** for interactive development:

```bash
cd sidecar/clj
clj -M -m sidecar.repl
```

Connect your editor to `localhost:7888` or use:
```bash
lein repl :connect 7888
# or
clj -M:nrepl
```

Quick verification commands:
- `(analyze-logs "path")` - Parse and analyze log files
- `(validate-config config)` - Validate configuration structure
- `(help)` - Show available commands


## /minikanren/

**REMOVED: miniKanren** - Risk of generating scenarios instead of proving constraints


## /z3/

Z3 SMT solver for:

* Proving system constraints hold
* Finding constraint violations  
* Verifying architectural decisions

Use Z3 to prove properties about your actual system, not theoretical models.

If you’re worried about invariants being violated, add/extend a Z3 case and run the Z3 harness via Clojure. Summarise only the invariant and whether it holds. but please don’t dump solver logs at me.


## Removed Tools

**Removed sections:**
- `/datalog/` - Risk of creating relationship models instead of checking real relationships  
- `/cue/` - Prefer simple validation scripts over schema generation
- `/fuzzing/` - Risk of property-based generation creating convincing fakes

**Focus on:** Z3 for mathematical proofs, Clojure for disposable analysis, EDN for clear data storage.


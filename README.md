# Resource Planning Ledger

[![CI](https://github.com/YOUR_GITHUB_USERNAME/resource-planning-ledger/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_GITHUB_USERNAME/resource-planning-ledger/actions/workflows/ci.yml)

**Live URL:** https://YOUR_APP.onrender.com

A Resource Planning Ledger built with Java 17 + Spring Boot 3, following a four-layer architecture (Controller → Manager → Engine → Repository) and four OO design patterns.

---

## Running Locally

### With Docker Compose (recommended)

```bash
docker compose up --build
```

App will be available at http://localhost:8080.

### Standalone Docker run (after building)

```bash
# Start PostgreSQL first
docker run -d --name rpl-db \
  -e POSTGRES_DB=rpl -e POSTGRES_USER=rpl -e POSTGRES_PASSWORD=rpl \
  -p 5432:5432 postgres:16-alpine

# Build and run app
docker build -t rpl .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/rpl \
  -e SPRING_DATASOURCE_USERNAME=rpl \
  -e SPRING_DATASOURCE_PASSWORD=rpl \
  rpl
```

---

## Design Patterns

### State — `ActionStateMachine`
`ProposedAction` holds a `stateName` string resolved at runtime to a stateless Spring singleton `ActionState` bean via `ActionStateMachineEngine`. Each state class (`ProposedState`, `InProgressState`, `SuspendedState`, `CompletedState`, `AbandonedState`) encapsulates its own legal transitions and throws `IllegalStateTransitionException` for illegal ones. Adding a new state in Week 2 requires only one new class file plus at most one line in the single existing state that gains a new outgoing edge.

### Composite — `PlanNode` tree
`Plan` (composite) and `ProposedAction` (leaf) both implement `PlanNode` and extend `PlanNodeEntity` (JOINED JPA inheritance). `Plan.getStatus()` is derived from children per the spec rules. `getTotalAllocatedQuantity()` recurses through descendants. The `accept(PlanNodeVisitor)` method is included on every node in Week 1 so that Week-2 Visitor implementations require zero changes to existing node classes.

### Iterator — `DepthFirstPlanIterator`
A pure-Java stack-based `Iterator<PlanNode>` that performs depth-first pre-order traversal over an already-loaded in-memory tree. No JPA queries inside `next()`. `PlanManager` loads the full subtree before handing it to the iterator. All clients that traverse the tree use this iterator; no manual child recursion is permitted.

### Template Method — `AbstractLedgerEntryGenerator`
The ledger-entry generation skeleton (`generateEntries`) is `final` and cannot be overridden, guaranteeing double-entry conservation in `postEntries` (also `final`). Subclasses implement `selectAllocations()` and `validate()`, and may override the `afterPost()` hook (empty in Week 1). `LedgerEngine` injects `List<AbstractLedgerEntryGenerator>` — new generators (e.g., Week-2 `AssetLedgerEntryGenerator`) are added as new `@Component` subclasses with zero changes to existing code.

---

## Render.com Setup

1. Create a **Web Service** → set build to Docker → port 8080.
2. Create a **PostgreSQL** database (free tier).
3. Add environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
4. Copy the deploy-hook URL into GitHub secret `RENDER_DEPLOY_HOOK`.

# CLAUDE.md — SpeakOut project memory

This file carries context for AI agents working in this repo, so any group member's
assistant picks up where the last session left off without re-deriving it. Human-facing
docs are in `README.md` (setup, demo accounts, feature map) and `docs/TECHNICAL.md`
(architecture explained for someone new to the stack) — read those first for "how does
this work." This file is for "why is it built this way" and "what's still in flight."

## What this is

SpeakOut: a CSC584 Enterprise Programming group assignment (UiTM, Group 3). An anonymized
school-bullying reporting and case-tracking web app for Malaysian secondary schools.
Students submit reports anonymously (identity hidden from peers, visible only to their
school's staff); teachers investigate and manage cases; admins get cross-school analytics.
Mandatory stack per the assignment brief: JSP + Servlet + JavaBean MVC, PostgreSQL,
GlassFish 8, no other framework substitutions (no Spring, no Node, no React).

The full original build spec (roles, screens, schema rationale, grading rubric) is in
`FABLE_BUILD_PROMPT.md` at the repo root — read it if you need the *original intent*
behind a screen or table, since it's more detailed than the README.

## Architecture conventions — follow these, don't reinvent them

- Packages by role: `com.speakout.bean` (data-only classes, suffixed `*Bean`),
  `com.speakout.controller` (one servlet per action + `AuthFilter`), `com.speakout.dao`
  (all SQL lives here, nowhere else), `com.speakout.util` (`DBConnection`, `Flash`, `Uploads`).
- **No `@WebServlet`/`@WebFilter`/`@MultipartConfig` annotations.** Every servlet, the
  auth filter, and multipart upload limits are declared in `src/main/webapp/WEB-INF/web.xml`
  with `<servlet>`/`<servlet-mapping>` pairs. This was a deliberate refactor (see History
  below) — don't reintroduce annotations when adding new servlets; add a web.xml entry instead.
- **No JSP scriptlets.** Views use JSTL (`<c:if>`, `<c:forEach>`) and EL (`${...}`) only.
  All logic happens in the servlet before it forwards to the JSP.
- **DB access:** `DBConnection.createConnection()` (static factory, `Class.forName` +
  `DriverManager`, not a connection pool/JNDI DataSource) reads `src/main/resources/db.properties`
  so each teammate can point at their own local Postgres login. Every DAO method wraps its
  `Connection`/`PreparedStatement`/`ResultSet` in try-with-resources.
- **Controller → forward vs redirect:** validation failures `forward()` back to the same
  JSP with errors attached to the request (preserves what the user typed); successful
  mutations `sendRedirect()` to a fresh GET (prevents duplicate-submit-on-refresh) with a
  one-shot flash message (`Flash.success`/`Flash.error`, stored in session, cleared by
  `nav.jspf` after one render).
- **Every Update/Delete needs a confirm step + success message** — this is explicitly
  graded. The pattern: any `<form>` with a `data-confirm="..."` attribute automatically
  gets a Bootstrap confirmation modal via shared JS in `WEB-INF/jsp/fragments/footer.jspf`.
- JSPs live under `WEB-INF/jsp/` (not directly in `webapp/`) so they can't be requested
  directly, only reached via a servlet forward.

## Key decisions and their reasons

- **8 tables, not the assignment's 4-table minimum or the proposal's inconsistent 5/4-table
  split.** Added `case_handlers` (co-handler join table, not in the original spec) because
  the mockups' "add co-handler" feature had nowhere else to live. Full rationale for the
  other 7 tables is in `FABLE_BUILD_PROMPT.md` §6.
- **IDs are app-generated display strings** (`SO-0421`, `CS-0409`) via Postgres sequences
  (`report_seq`, `case_seq`, etc. in `schema.sql`), not raw integers — they appear directly
  in the UI as "Case IDs" so they needed to look like one.
- **A case is auto-created on report submission**, assigned to the school's least-loaded
  teacher (fewest open cases) via `UserDAO.teachersOfSchool()`. If a school has zero
  registered teachers, the report just sits at `Submitted` with no case until one exists.
- **Report status and case status are separate columns**, kept roughly in sync by the
  controller (`CaseStatusServlet`) rather than derived from one another — a case can be
  reassigned/reopened independently of what the student-facing report status shows.
- **Evidence files are stored outside the WAR**, at `~/speakout-uploads/{reportId}/`, so
  redeploying (`asadmin deploy --force`) never deletes previously uploaded files. The DB
  only stores the relative path/type/size.
- **PostgreSQL instead of MySQL** (the brief's default) — same JDBC API either way, just a
  different driver/URL, and it's what the team already has installed locally.
- **PIBG/PDRM roles exist only in the `users.role` CHECK constraint**, with no dedicated
  screens — the brief treats them as read-only oversight variants of Teacher, and building
  their UI was explicitly deferred rather than guessed at.

## History — how we got here (chronological)

1. **Initial build** — full first draft built from `FABLE_BUILD_PROMPT.md`: 8-table schema,
   all DAOs/beans/servlets/JSPs, seeded with 25 real Selangor schools (from the MOE CSV in
   `data/`) and demo reports/cases spanning Feb–Jun 2026. Originally scaffolded in a sibling
   `speakout_v2/` directory, then moved into this directory (`CSC584_GROUP/`) at the
   student's request to match the layout of the individual assignment. `speakout_v2/` now
   holds only the HTML mockups (GitHub Pages), not the Java app.
2. **Syllabus-alignment refactor** — the codebase was originally built with modern Jakarta
   idioms (`@WebServlet` annotations, `model`/`web` package names). It was refactored to
   match this course's specific lecture-note conventions: `bean`/`controller`/`dao`/`util`
   packages, `DBConnection.createConnection()`, and `web.xml`-only servlet mapping (see
   "Architecture conventions" above). Behavior did not change — this was a pure rename/
   restructure. If you're an agent reading this in a fresh session: **this is now the
   correct, permanent structure** — do not "modernize" it back to annotations.
3. **Two bug fixes**, still valid, watch for regressions:
   - Editing a **Draft** report used to silently drop the evidence-upload field (the form
     only rendered `<input type="file">` in create mode, and the edit servlet had no
     multipart handling). Fixed: `report-form.jsp` is multipart in both modes and shows
     already-uploaded files; `ReportEditServlet` saves new uploads via the same
     `ReportFormServlet.saveEvidence()` helper.
   - The case timeline (report detail page) rendered events grouped by type instead of
     time order, so a note added after a case was marked "Resolved" appeared *before* the
     resolution event. Fixed: `ReportViewServlet.buildTimeline()` now sorts the assembled
     event list by timestamp before returning it. If you add a new timeline event type,
     make sure it goes through the same sort, not appended unsorted at the end.
4. **Docs pass** — `README.md`, `docs/TECHNICAL.md`, and this file were written/rewritten
   to onboard both a grading lecturer and a Java-EE-unfamiliar engineer. Note: `README.md`
   deliberately avoids mentioning course lecture materials by name (it's shared with the
   lecturer) — describe the architecture in plain engineering terms there, not as "what
   Chapter N teaches."

## Current status / what's left

- Working end-to-end: auth (register/login/logout + session gate), student flow (submit/
  edit/delete reports with evidence upload, dashboard, My Reports, report detail/timeline,
  profile), teacher/admin flow (cases list, case management with notes/co-handlers/status/
  delete, admin analytics dashboard with live Chart.js queries).
- **Not done:** PIBG/PDRM screens, real forgot-password flow (currently a JS `alert()`
  stub), user manual (a course deliverable, separate from this codebase), automated tests,
  CI/CD.
- **Not yet committed to git** as of this writing — the student intends to turn this
  directory into its own repository. If you're an agent picking this up after that point,
  check `git log` for anything that happened after this file was last updated before
  trusting the History section above as complete.

## Local dev quick reference

```bash
createdb speakout && psql -d speakout -f db/schema.sql && psql -d speakout -f db/seed.sql
./mvnw package
~/glassfish8/bin/asadmin start-domain
~/glassfish8/bin/asadmin deploy --force target/speakout.war
# http://localhost:8080/speakout/  (health check: .../speakout/health)
```

Demo accounts and the full setup/feature walkthrough are in `README.md`.

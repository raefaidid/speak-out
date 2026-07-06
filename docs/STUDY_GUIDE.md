# SpeakOut — Codebase Study Guide (mapped to CSC584 lecture notes)

This document breaks down the SpeakOut codebase directory-by-directory and maps each
part to the corresponding CSC584 Enterprise Programming lecture chapter, for study
purposes. It complements `docs/TECHNICAL.md` (which explains the stack from first
principles) by answering: *where do I find Chapter N's ideas in this repo, and how
important is that part to read?*

---

## Overview

SpeakOut is a Jakarta EE **JSP + Servlet + JavaBean MVC** web app (no Spring/React/Node
— mandated by the assignment brief) for anonymized school-bullying reporting. Students
submit reports; teachers manage cases; admins see cross-school analytics. Stack: JDK 21,
Jakarta EE 11, GlassFish 8, PostgreSQL, Maven WAR packaging. It's a near 1:1 realization
of the course syllabus — almost every directory corresponds to one lecture chapter's
pattern.

---

## Directory-by-directory breakdown

### `src/main/java/com/speakout/bean/` — Chapter 6 (Enterprise JavaBean components)

Plain data classes (`ReportBean`, `CaseBean`, `UserBean`, `SchoolBean`, `EvidenceBean`,
`CategoryBean`, `CaseNoteBean`): private fields + getters/setters, `Serializable`, no
logic beyond small derived helpers (e.g. `ReportBean.isEditable()`, `getStatusPill()`).
This is the textbook "JavaBean" convention Chapter 6 teaches — value holders passed
between DAO → Servlet → JSP.

**Study priority: high** — this is the simplest chapter's concept but appears
everywhere; good place to start.

### `src/main/java/com/speakout/dao/` — Chapter 5 (Java Database Connectivity / JDBC)

One class per table (`UserDAO`, `ReportDAO`, `CaseDAO`, `CaseNoteDAO`, `EvidenceDAO`,
`SchoolDAO`, `CategoryDAO`, `AnalyticsDAO`). All raw SQL lives here — `PreparedStatement`,
`ResultSet` mapping to Beans, try-with-resources for connection safety. `CaseDAO`
(~10KB) and `ReportDAO` (~8.4KB) are the biggest/most complex — worth reading closely
since they show joins, transactions, and the auto-case-assignment logic.

**Study priority: high** — this is the core JDBC chapter applied at real scale.

### `src/main/java/com/speakout/util/DBConnection.java` — Chapter 7-2 (Development of Enterprise Application)

Static `createConnection()` using `Class.forName` + `DriverManager` — literally the
lecture's connection pattern (the file's own comment cites "Chapter 7-2 pattern"). One
deviation: URL/credentials resolve from env vars, falling back to `db.properties`, so
it's configurable without editing Java.

**Study priority: medium** — short file, but read it to see how the lecture pattern
gets adapted for a real team (not hardcoded credentials).

### `src/main/java/com/speakout/controller/` — Chapter 3 (Servlets) + Chapter 7-1 (Development of Enterprise Application)

20 servlets, one per action (`LoginServlet`, `ReportFormServlet`, `CaseServlet`,
`AnalyticsServlet`, etc.) plus `AuthFilter` (a `Filter`, also Ch.3 territory). Each
servlet: reads request params → calls a DAO → sets request attributes → `forward()`/
`sendRedirect()` to a JSP. This is the "Controller" in MVC — Chapter 7-1's
architectural pattern made concrete. `ReportFormServlet` (~6.6KB) and
`ReportEditServlet`/`ReportViewServlet` are the most involved (multipart file upload +
timeline assembly).

**Study priority: highest** — biggest package, most logic, ties every other chapter
together.

### `src/main/webapp/WEB-INF/web.xml` — Chapter 8 (Packaging and Deployment of Enterprise Application)

The deployment descriptor: every servlet/filter/multipart-config declared
declaratively (no `@WebServlet` annotations — deliberate, to match how the course
teaches it). This *is* Chapter 8's subject matter as a literal artifact.

**Study priority: high** — short file, directly maps to a whole chapter; read
alongside `pom.xml` (WAR packaging) and `Dockerfile`/`DEPLOYMENT.md` for the full
deployment picture.

### `src/main/webapp/WEB-INF/jsp/` — Chapter 4 (JSP)

- `login.jsp`, `register.jsp`, `profile.jsp` (top-level, shared across roles)
- `student/` — dashboard, reports list, report form, report detail (4 files)
- `staff/` — cases list, case detail, analytics (3 files)
- `fragments/` — `head.jspf`, `nav.jspf`, `footer.jspf` (JSP includes, reused across
  pages)

Uses JSTL/EL only (`<c:if>`, `<c:forEach>`, `${...}`) — **no scriptlets**, which
Chapter 4 presents as the modern/correct JSP style vs. old scriptlet-heavy JSP.

**Study priority: medium-high** — good for seeing JSTL in a real UI, but less
conceptually new than servlets/JDBC if you already understand MVC.

### `src/main/webapp/assets/style.css`, `index.jsp`

Static CSS and the welcome file. Not tied to a specific chapter — supporting material
only.

**Study priority: low.**

### `db/schema.sql`, `db/seed.sql` — Chapter 5 (JDBC) supporting material

8 tables: `school`, `users`, `category`, `reports`, `cases`, `case_notes`, `evidence`,
`case_handlers`. Not Java, but essential to understand the DAOs — read this *before*
the DAO package.

**Study priority: high, but as a prerequisite read, not standalone.**

### `data/`, `mockup/` — project inputs, not course material

CSV of real Selangor schools (seed data source) and UI mockup screenshots used as the
design reference. No lecture-chapter mapping.

**Study priority: low** — context only.

### Root docs: `CLAUDE.md`, `README.md`, `docs/TECHNICAL.md`, `FABLE_BUILD_PROMPT.md`, `DEPLOYMENT.md`

`CLAUDE.md` is the most valuable for study purposes — it explicitly documents *which
lecture chapter's pattern* each architectural choice follows and why (e.g. web.xml-only
mapping, `DBConnection` style). `docs/TECHNICAL.md` explains the architecture in plain
engineering terms for someone new to the stack. `FABLE_BUILD_PROMPT.md` has the
original spec/schema rationale.

**Study priority: read `CLAUDE.md` first** — it's effectively a study guide someone
already wrote for this exact purpose.

### Chapter 1 and Chapter 2 — not represented as a distinct directory

Chapter 1 (Review of OOP) and Chapter 2 (Introduction to Java EE) are foundational —
you'll see their concepts (classes, interfaces, the Jakarta EE platform itself via
`pom.xml`'s `jakarta.jakartaee-web-api` dependency) throughout, but there's no single
folder that "is" Chapter 1/2 the way `bean/` maps to Ch.6.

---

## Suggested study order

1. `CLAUDE.md` (orientation)
2. `db/schema.sql` (data model)
3. `bean/` (Ch.6)
4. `dao/` (Ch.5)
5. `controller/` (Ch.3, Ch.7-1)
6. `jsp/` (Ch.4)
7. `web.xml` + `pom.xml` (Ch.8)
8. `util/DBConnection.java` (Ch.7-2) — last, since it's a small piece you'll already
   understand by then.

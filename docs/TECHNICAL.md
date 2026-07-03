# SpeakOut — Technical Documentation

This document is for an engineer joining the project with **no prior Java EE / Jakarta EE
experience**. It explains the technology stack from first principles, then walks through
the actual codebase.

---

## 1. The tech stack, explained

### What is a "Servlet"?

A **Servlet** is a Java class that handles one HTTP request/response cycle. Think of it as
the Java equivalent of an Express route handler (Node) or a Django view function — except
it's a class, not a function, and a web server ("container") manages its lifecycle for you.

```java
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // read form fields, do work, decide what HTML to send back
    }
}
```

- `doGet` handles GET requests, `doPost` handles POST requests. You override whichever
  ones your endpoint needs.
- `HttpServletRequest` gives you the incoming request (form fields via `getParameter()`,
  the logged-in user's session via `getSession()`, etc.).
- `HttpServletResponse` is what you write the reply into (or, more commonly in this app,
  what you redirect from).

### What is "JSP"?

**JSP (JavaServer Pages)** is a templating language for generating HTML, conceptually
similar to EJS, Jinja2, or ASP. A `.jsp` file is HTML with special tags for showing dynamic
data, loops, and conditionals. It gets compiled into a servlet by the container before it
runs — you never see that generated code.

This project uses two JSP tag libraries instead of writing raw Java inside pages:

- **EL (Expression Language)** — `${...}` syntax for reading values, e.g. `${user.fullName}`
  reads the `fullName` property off a `user` object (calls its `getFullName()` getter).
- **JSTL (JSP Standard Tag Library)** — `<c:if>`, `<c:forEach>`, `<c:choose>` etc. for
  control flow, imported at the top of a page with `<%@ taglib prefix="c" uri="jakarta.tags.core" %>`.

```jsp
<c:forEach var="r" items="${reports}">
  <tr><td>${r.title}</td><td>${r.status}</td></tr>
</c:forEach>
```

You will **not** find `<% ... %>` scriptlet blocks (raw Java embedded in HTML) anywhere in
this codebase — that style is considered bad practice today, and the assignment brief
explicitly forbids it. All logic lives in Java classes; JSPs only display data that's
already been prepared for them.

### What is a "JavaBean"?

A **JavaBean** here just means a plain Java class that holds data: private fields, a
no-argument constructor, and public `getX()`/`setX()` methods for each field. No business
logic. JSP's EL syntax (`${user.fullName}`) relies on this getter/setter convention to
read and write bean properties.

```java
public class UserBean {
    private String fullName;
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    // ...
}
```

### What is "MVC" here, concretely?

| Layer | Technology | Folder | Job |
|---|---|---|---|
| **Model** | JavaBeans + DAOs | `bean/`, `dao/` | Hold data; talk to the database |
| **View** | JSP | `webapp/WEB-INF/jsp/` | Render HTML from data the controller handed it |
| **Controller** | Servlets | `controller/` | Handle the request, call the DAO, decide which view to show |

A request never skips a layer: browser → **Controller** (Servlet) → **Model** (DAO → DB)
→ back to Controller → **View** (JSP) → browser.

### What is `web.xml`?

Instead of annotating each servlet with its URL (`@WebServlet("/login")`), this project
declares every servlet, filter, and their URLs in one central XML file:
`src/main/webapp/WEB-INF/web.xml`. This is the older, more explicit style — every mapping
lives in one place instead of being scattered across 19 files, which makes it easy to see
the whole routing table at a glance.

```xml
<servlet>
  <servlet-name>Login</servlet-name>
  <servlet-class>com.speakout.controller.LoginServlet</servlet-class>
</servlet>
<servlet-mapping>
  <servlet-name>Login</servlet-name>
  <url-pattern>/login</url-pattern>
</servlet-mapping>
```

The `<servlet-name>` is just an internal label connecting the two blocks — it never
appears in a URL.

### What is GlassFish?

**GlassFish** is the application server ("container") that actually runs the compiled
code. It implements the Jakarta EE APIs (Servlet, JSP) so your classes don't need a
`main()` method or their own HTTP listener — GlassFish provides both, plus session
management, and calls your servlets' `doGet`/`doPost` when a matching request arrives.
The app is packaged as a **WAR** file (`.war` — a zip of compiled classes + JSPs + config)
and deployed into GlassFish with `asadmin deploy`.

### What is JDBC?

**JDBC (Java Database Connectivity)** is Java's standard API for talking to a SQL
database. There's no ORM in this project (no Hibernate/JPA) — every query is written by
hand as a `String` of SQL and run through `PreparedStatement`, which is JDBC's mechanism
for parameterized queries (the `?` placeholders below), which also protects against SQL
injection:

```java
try (Connection c = DBConnection.createConnection();
     PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE email = ?")) {
    ps.setString(1, email);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            String name = rs.getString("full_name");   // read a column by name
        }
    }
}
```

`try (...)` here is a **try-with-resources** block — it automatically closes the
`Connection`, `PreparedStatement`, and `ResultSet` when the block exits, even if an
exception is thrown. This is why every DAO method in this project is wrapped this way.

---

## 2. Directory tour

```
CSC584_GROUP/
├── pom.xml                          Maven build file — dependencies, Java version, WAR packaging
├── mvnw, mvnw.cmd, .mvn/            Maven Wrapper — lets you build without installing Maven
├── db/
│   ├── schema.sql                   Table definitions (run once to create the DB)
│   └── seed.sql                     Demo data (schools, users, sample reports/cases)
└── src/main/
    ├── resources/
    │   └── db.properties            DB connection string + credentials (per-developer, edit locally)
    ├── java/com/speakout/
    │   ├── bean/                    Data-only classes: UserBean, ReportBean, CaseBean, ...
    │   ├── controller/               One servlet per action/page (19 total) + AuthFilter
    │   ├── dao/                      One DAO per table/feature — the only place SQL lives
    │   └── util/                     DBConnection, Flash (success/error banners), Uploads
    └── webapp/
        ├── index.jsp                 Redirects to /dashboard
        ├── assets/style.css          Shared stylesheet
        └── WEB-INF/
            ├── web.xml               Every servlet/filter/multipart-upload mapping
            ├── jsp/                  All page templates
            │   ├── fragments/         Shared <head>, nav bar, footer (included on every page)
            │   ├── login.jsp, register.jsp, profile.jsp
            │   ├── student/           Student-only pages
            │   └── staff/             Teacher/Admin pages
```

**Rule of thumb:** if you're adding a feature, you'll usually touch four files — one bean
(if new data), one DAO method (the SQL), one servlet (the logic), one JSP (the display) —
plus a `<servlet>`/`<servlet-mapping>` pair in `web.xml`.

---

## 3. A request from end to end: logging in

This is the clearest example of every layer working together.

1. **Browser** — `login.jsp` renders an HTML `<form method="post" action=".../login">`.
2. **Routing** — `web.xml` maps the URL `/login` to `com.speakout.controller.LoginServlet`.
3. **Controller** (`LoginServlet.doPost`):
   ```java
   String email = req.getParameter("email");
   String password = req.getParameter("password");
   UserBean user = userDAO.findByEmail(email);
   if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
       // set an error message as a request attribute, forward back to the form
   }
   ```
4. **Model / DAO** (`UserDAO.findByEmail`) — opens a connection via
   `DBConnection.createConnection()`, runs a `PreparedStatement` SELECT, and maps the
   single row it finds into a new `UserBean` object.
5. **Back in the controller** — on success:
   ```java
   HttpSession session = req.getSession(true);
   session.setAttribute("user", user);          // the whole bean, not just an ID
   resp.sendRedirect(req.getContextPath() + homeFor(user));   // role-based landing page
   ```
   On failure, it forwards (not redirects) back to the JSP with an error message attached
   to the request, so the form re-renders with the invalid email pre-filled and a message
   shown.
6. **View** — every subsequent page reads `${sessionScope.user.fullName}` etc. directly
   from the session-stored bean via EL — no re-querying the database just to render the nav bar.

Every other feature in the app (submit a report, add a case note, change a case status...)
follows this same five-step shape: **Controller reads input → DAO reads/writes the
database → Controller decides the outcome → forward or redirect → View renders it.**

---

## 4. Authentication & session handling

- `AuthFilter` is a **servlet Filter** — code that runs *before* a servlet, for every
  single request (`<url-pattern>/*</url-pattern>` in web.xml). It checks
  `session.getAttribute("user")` and redirects to `/login` if it's missing, except for a
  small allowlist of public paths (login, register, health check, static assets).
- Logging in stores the entire `UserBean` in the `HttpSession` — that's why JSPs can read
  `sessionScope.user.role`, `sessionScope.user.schoolName`, etc. without another DB query.
- Logging out (`LogoutServlet`) calls `session.invalidate()`, which destroys the session
  server-side and expires the browser's session cookie.
- Passwords are never stored or compared in plain text — `RegisterServlet` hashes with
  bcrypt (`BCrypt.hashpw`) before insert, and `LoginServlet` verifies with
  `BCrypt.checkpw`, which handles the salt internally.

---

## 5. Data model

Eight tables, all in `db/schema.sql`. IDs are human-readable strings generated from
Postgres sequences (e.g. `SO-0421` for a report, `CS-0409` for a case) rather than raw
integers, because they appear directly in the UI as "Case IDs."

```
school ──< users ──< reports >── category
             │           │
             │           └──1:1── cases ──< case_notes
             │                       │
             └───(assigned_to)───────┤
                                      └──< case_handlers >── users (co-handlers)
reports ──< evidence
```

- **school** — one row per school; `code` is what students type at registration.
- **users** — one table for every role (`Student`, `Teacher`, `Admin`, plus `PIBG`/`PDRM`
  which exist in the data model but have no dedicated screens yet). `school_id` scopes a
  user to one school (multi-tenant boundary).
- **category** — bullying type (Verbal, Cyber, Physical, Social) — a lookup table.
- **reports** — what a student submits. `status` lifecycle: `Draft → Submitted → In review → Resolved`.
- **cases** — created automatically when a report is submitted (assigned to the school's
  least-loaded teacher). `status` lifecycle: `New → Under Investigation → Resolved/Closed`.
  This is a *separate* status from the report's, deliberately: a case can be reopened or
  reassigned independently of what the student sees as their report's status (the
  controller keeps the two roughly in sync when a teacher changes case status).
- **case_notes** — an append-only timestamped log per case. `visibility` controls whether
  the student can see the note (`reporter-visible`) or only staff can (`internal`).
- **case_handlers** — join table for adding a second/third teacher to a case beyond the
  primary `assigned_to`.
- **evidence** — uploaded files attached to a report; the actual bytes live outside the
  database (see §7), this table just stores the path/type/size.

`CHECK` constraints (e.g. `status IN ('Draft', 'Submitted', ...)`) are used instead of
Postgres native `ENUM` types, purely so JDBC code can pass plain Java Strings without
extra casting.

---

## 6. Validation

Two layers, and only one of them is trustworthy:

1. **Client-side (JavaScript)** — runs in the browser on blur/submit, for instant feedback
   (e.g. "passwords don't match"). This exists purely for UX; it is trivially bypassed by
   anyone using curl or disabling JS.
2. **Server-side (Java, authoritative)** — every servlet that accepts a form re-validates
   everything from scratch. The pattern used everywhere (see `RegisterServlet` or
   `ReportFormServlet.readForm`):
   ```java
   Map<String, String> errors = new LinkedHashMap<>();
   if (title.isEmpty()) errors.put("title", "Please give your report a short title.");
   // ... more checks ...
   if (!errors.isEmpty()) {
       req.setAttribute("errors", errors);
       req.getRequestDispatcher(".../form.jsp").forward(req, resp);   // re-render with messages
       return;
   }
   ```
   The JSP then shows `${errors.title}` next to the offending field. Nothing is trusted
   from the client — the server re-checks types, lengths, foreign-key existence (e.g. the
   school code), and uniqueness (e.g. email) independently of whatever the browser did.

---

## 7. File uploads

Evidence files (screenshots, videos, documents attached to a report) use the Servlet API's
built-in multipart support — no external upload library.

- `web.xml` declares a `<multipart-config>` block on the two servlets that accept uploads
  (`ReportForm`, `ReportEdit`), capping file size at 50MB.
- The servlet reads uploaded parts with `request.getParts()` and filters for the field
  named `evidence`.
- `Uploads.save()` writes each file to `~/speakout-uploads/{reportId}/{filename}` —
  **deliberately outside the deployed WAR**, so redeploying the app never deletes
  previously uploaded evidence.
- `EvidenceDAO` stores only the relative path, MIME-derived type, and size in the database
  — the DB never holds file bytes.
- `EvidenceServlet` streams a file back on request, after checking the requester is either
  the report's owner or a staff member (Teacher/Admin) — this is the only place file
  bytes leave the server.

---

## 8. Local development setup

Prerequisites: JDK 21, PostgreSQL, GlassFish 8 (unzipped anywhere on disk).

```bash
# Database
createdb speakout
psql -d speakout -f db/schema.sql
psql -d speakout -f db/seed.sql

# Point the app at your Postgres login if it isn't a passwordless local user
# named after your OS account — edit src/main/resources/db.properties

# Build (the wrapper downloads Maven itself — no local install needed)
./mvnw package

# Deploy
~/glassfish8/bin/asadmin start-domain
~/glassfish8/bin/asadmin deploy --force target/speakout.war
```

App: `http://localhost:8080/speakout/` · DB connectivity check: `.../speakout/health`

Demo logins are listed in the root `README.md`.

**Re-deploying after a code change:** re-run `./mvnw package` then the `asadmin deploy
--force` command — GlassFish hot-swaps the WAR. There is no separate "dev mode"; every
change requires a rebuild+redeploy cycle (typically a few seconds).

**Resetting data:** re-run both `.sql` files against the `speakout` database; this drops
and recreates every table (`schema.sql` starts with `DROP TABLE ... CASCADE`).

---

## 9. Adding a new feature — a worked recipe

Say you want to add a "mark report as spam" action for teachers. The shape is always the
same:

1. **DAO** — add a method to `ReportDAO`, e.g. `markSpam(String reportId)`, containing the
   `UPDATE` statement.
2. **Servlet** — create `SpamReportServlet` in `controller/`, extending `HttpServlet`,
   overriding `doPost`: check the user is staff, call the DAO method, set a success
   message via `Flash.success(session, "...")`, then `resp.sendRedirect(...)`.
3. **web.xml** — add a `<servlet>` / `<servlet-mapping>` pair, e.g. mapping
   `/report/spam` to the new class.
4. **JSP** — add a `<form method="post" action=".../report/spam">` button somewhere in
   `case-detail.jsp`, ideally with `data-confirm="..."` (see `footer.jspf` — any form with
   a `data-confirm` attribute automatically gets a confirmation modal via the shared JS
   there, and the flash message banner appears automatically on the next page via
   `nav.jspf`).

No annotations, no XML beyond the one `web.xml` block, no framework magic — every hop is
explicit and traceable by reading the four files above in order.

---

## 10. Testing approach

There is no automated test suite. Verification during development has been manual,
end-to-end HTTP testing with `curl` against the deployed app — logging in as each role,
exercising create/update/delete flows, and checking role-guard redirects and database
state directly with `psql`. If you add automated tests, `HttpServletRequest`/`Response`
mocking (e.g. with Mockito) around each servlet's `doGet`/`doPost` is the natural
entry point; DAOs can be tested against a real local Postgres instance since there's no
abstraction layer to mock underneath them.

---

## 11. Known gaps

- `PIBG` and `PDRM` roles exist in the `users.role` check constraint but have no
  dedicated screens — only Student, Teacher, and Admin are implemented.
- "Forgot password" on the login page is a client-side stub (shows an alert); there is no
  password-reset flow.
- No automated tests (see §10).
- No CI/CD pipeline configured yet.

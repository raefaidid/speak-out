# SpeakOut — CSC584 Enterprise Programming (Group 3)

Anonymized school bullying reporting and case tracking. JSP + JSTL views, Servlet controllers,
JavaBean/DAO model, PostgreSQL, GlassFish 8.

## Run it

Prereqs: JDK 21, PostgreSQL running locally, GlassFish 8. No Maven install needed — `./mvnw` downloads it.

```bash
# 1. Database (once, or to reset all data)
createdb speakout
psql -d speakout -f db/schema.sql
psql -d speakout -f db/seed.sql

# 2. Credentials: copy src/main/resources/db.properties.example to db.properties
#    and edit it for your local postgres user (db.properties is gitignored).

# 3. Build & deploy
./mvnw package
~/glassfish8/bin/asadmin start-domain
~/glassfish8/bin/asadmin deploy --force target/speakout.war
```

Open **http://localhost:8080/speakout/** (DB smoke test: `/health`).

## Demo accounts (seeded)

| Role | Email | Password |
|---|---|---|
| Admin | admin@speakout.demo | Admin@123 |
| Teacher (SMK Raja Mahadi) | teacher.aina@speakout.demo | Teacher@123 |
| Teacher (SMK Raja Lumu) | teacher.farid@speakout.demo | Teacher@123 |
| Student (SMK Raja Mahadi) | student.aiman@speakout.demo | Student@123 |
| Student (SMK Raja Mahadi) | student.mei@speakout.demo | Student@123 |

The login page has quick-preview buttons for the first Student/Teacher/Admin account.
Registration validates against seeded school codes (25 Selangor schools, e.g. `BEA0091`).

## Layout

- `src/main/java/com/speakout/bean` — JavaBeans
- `src/main/java/com/speakout/dao` — DAO classes (all SQL lives here)
- `src/main/java/com/speakout/controller` — Servlets + AuthFilter (validation, session, routing)
- `src/main/webapp/WEB-INF/jsp` — JSP views; `fragments/` holds the shared nav/footer
- `src/main/webapp/WEB-INF/web.xml` — every servlet/filter mapping
- `db/` — schema + seed scripts
- `docs/TECHNICAL.md` — architecture walkthrough for engineers new to this stack
- Evidence uploads are stored in `~/speakout-uploads/` (outside the WAR).

See `CLAUDE.md` for build history, key decisions, and current status.

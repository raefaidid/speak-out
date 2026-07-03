# Deployment Guide
## SpeakOut — CSC584 Group 3

This mirrors the approach used for `CSC584_INDIVIDUAL/DEPLOYMENT.md`, adjusted for two
things that project didn't have to deal with: the assignment mandates **GlassFish 8**
specifically (not "any servlet container"), and SpeakOut's `pom.xml` targets **Jakarta EE 11**
(Servlet 6.1) — a version newer than what Tomcat/Jetty currently implement. So the container
image here runs actual GlassFish 8 instead of Tomcat.

---

## What changed to make this deployable

1. **`DBConnection.java` now reads env vars first.** It still falls back to
   `db.properties` for local dev, but in production it reads `DB_URL` / `DB_USER` /
   `DB_PASSWORD` from the environment — same pattern as `Db.java` in the individual
   project. Nothing else about local dev changes.
2. **`Dockerfile`** builds the WAR with Maven, then installs GlassFish 8.0.3 (official
   build from `eclipse-ee4j/glassfish` on GitHub) into a second stage and deploys the WAR
   into it as context root `/speakout` — matching the local `http://localhost:8080/speakout/`
   URL you already use.
3. **`docker-entrypoint.sh`** starts the domain, points GlassFish's HTTP listener at the
   `PORT` env var (defaulting to 8080), deploys the WAR, then restarts the domain in the
   foreground so the container has a long-running process to track.

## Before you deploy: one thing to know about uploads

Evidence files are saved to `~/speakout-uploads/{reportId}` (outside the WAR, per
`CLAUDE.md`). On a plain container host (Railway, Render) that directory lives inside the
container's writable layer and is **wiped on every redeploy** — fine for demoing the app,
not fine for real persistence. If you need uploads to survive redeploys, either:
- use DigitalOcean/Hetzner (Option C/D below) and mount a real disk path, or
- attach a persistent volume if your platform offers one (Railway does, under "Volumes").

This isn't something the code needs to change for — it's a hosting choice.

---

## Step 1 — Push your code to GitHub

```bash
cd /Users/raefdd/uitm-degree/enterprise-programming/projects/CSC584_GROUP

git init
git add .
git commit -m "Initial commit — SpeakOut MVP"

# Create a repo on github.com, then:
git remote add origin https://github.com/YOUR_USERNAME/speakout.git
git push -u origin main
```

> `target/` and `db.properties` are already gitignored — don't force-add either.

---

## Option A: Railway — Recommended (Free)

Railway builds directly from a `Dockerfile` in your repo, so no manual "start command"
config is needed (that's simpler than the individual project's Jetty-based setup).

1. Sign up at [railway.app](https://railway.app) with GitHub.
2. **New Project → Deploy from GitHub repo** → select `speakout`. Railway detects the
   `Dockerfile` and builds it automatically.
3. **Add a PostgreSQL database**: inside the project, **+ New → Database → Add PostgreSQL**.
4. **Set environment variables** on your app service (not the DB) → **Variables** tab:

   | Key | Value |
   |-----|-------|
   | `DB_URL` | `JDBC_DATABASE_URL` from the Postgres service (format `jdbc:postgresql://HOST:PORT/DB_NAME`) |
   | `DB_USER` | `PGUSER` from the Postgres service |
   | `DB_PASSWORD` | `PGPASSWORD` from the Postgres service |

   Railway also injects `PORT` automatically — `docker-entrypoint.sh` already reads it,
   no action needed.
5. **Load the schema**: use Railway's "Connect" command for the Postgres service to get a
   `psql` connection string, then:
   ```bash
   psql "<connection-string>" -f db/schema.sql
   psql "<connection-string>" -f db/seed.sql
   ```
6. Push to GitHub — Railway auto-deploys. You'll get a URL like
   `https://speakout.up.railway.app`. The app is served at the root context configured by
   Railway's routing, but internally it's still mounted at `/speakout` — Railway forwards
   `/` to whatever port GlassFish is listening on, so visiting the root URL works.

---

## Option B: Render — Free (with limits)

Render also builds from `Dockerfile` directly (**Environment: Docker**), same as Railway.
Its free Postgres expires after 90 days — fine for a graded demo period, not for anything
longer.

1. **New → Web Service** → connect your GitHub repo → Render auto-detects the `Dockerfile`.
2. **New → PostgreSQL** → copy the **Internal Database URL**
   (`postgresql://user:pass@host/dbname`) and convert to JDBC form:
   `jdbc:postgresql://host/dbname`.
3. Set `DB_URL` / `DB_USER` / `DB_PASSWORD` env vars on the web service (same values as
   Railway above).
4. Render requires your app to bind to the `PORT` it injects — already handled by
   `docker-entrypoint.sh`.
5. Run `db/schema.sql` and `db/seed.sql` against the DB via Render's dashboard "Connect" tab
   or any `psql` client.

---

## Option C: DigitalOcean Droplet — Low Budget (~USD 6/month)

Same idea as the individual project's guide, but you run GlassFish directly instead of
compiling a WAR and running Jetty.

```bash
ssh root@YOUR_DROPLET_IP

apt update && apt upgrade -y
apt install -y openjdk-21-jdk postgresql postgresql-contrib maven git unzip

# GlassFish 8
curl -fsSL -o /tmp/glassfish.zip \
  https://github.com/eclipse-ee4j/glassfish/releases/download/8.0.3/glassfish-8.0.3.zip
unzip -q /tmp/glassfish.zip -d /opt

# Database
su - postgres -c "psql -c \"CREATE DATABASE speakout;\""
su - postgres -c "psql -c \"CREATE USER speakoutapp WITH PASSWORD 'choose-a-strong-password';\""
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE speakout TO speakoutapp;\""

# Code
cd /opt
git clone https://github.com/YOUR_USERNAME/speakout.git
cd speakout
./mvnw clean package -DskipTests

# Env vars + deploy
export DB_URL="jdbc:postgresql://localhost:5432/speakout"
export DB_USER="speakoutapp"
export DB_PASSWORD="choose-a-strong-password"

/opt/glassfish8/bin/asadmin start-domain
psql -U speakoutapp -d speakout -f db/schema.sql
psql -U speakoutapp -d speakout -f db/seed.sql
/opt/glassfish8/bin/asadmin deploy --force --contextroot speakout target/speakout.war
```

App is live at `http://YOUR_DROPLET_IP:8080/speakout/`. To keep it running after you
disconnect, either run the above inside `screen`, or (cleaner) set the `DB_URL`/`DB_USER`/
`DB_PASSWORD` exports in a systemd service file that runs `asadmin start-domain --verbose`
on boot. Evidence uploads at `~/speakout-uploads` persist here since it's a real disk, not
an ephemeral container.

---

## Option D: Hetzner VPS — Cheapest Paid

Same steps as Option C — only the sign-up provider differs (`hetzner.com/cloud`,
~EUR 4/month CX22 plan).

---

## Quick Comparison

```
FREE OPTIONS
├── Railway    → Builds Dockerfile automatically, easiest, uploads are ephemeral
└── Render     → Same as Railway, free DB expires after 90 days

PAID OPTIONS
├── DigitalOcean → USD 6/mo, uploads persist on real disk, USD 200 new-user credit
└── Hetzner      → EUR 4/mo, same setup as DigitalOcean
```

**Recommendation:** Railway for the graded demo (fastest to a live URL, Docker build
means zero manual "start command" fiddling). If evidence-upload persistence matters for
grading, use DigitalOcean instead.

---

## Common Problems

| Problem | Likely cause | Fix |
|---------|-------------|-----|
| App starts but shows DB error | `DB_URL`/`DB_USER`/`DB_PASSWORD` not set, or `DBConnection` still only has old code | Check env vars in the platform dashboard; confirm you pulled the updated `DBConnection.java` |
| Build fails on GlassFish download | GitHub release URL changed or rate-limited | Check https://github.com/eclipse-ee4j/glassfish/releases for the current version and update the URL in `Dockerfile` |
| 404 at `/` | App is deployed at context root `/speakout`, not `/` | Visit `<host>/speakout/` — same as local dev |
| Uploaded evidence disappears after redeploy | Platform uses an ephemeral filesystem | Use a persistent volume (Railway) or a real VPS (Option C/D) |

---

*Guide written for SpeakOut MVP — July 2026*

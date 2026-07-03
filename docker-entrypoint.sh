#!/bin/sh
set -e

GF_HOME=/opt/glassfish8/bin
PORT="${PORT:-8080}"

"$GF_HOME/asadmin" start-domain domain1
"$GF_HOME/asadmin" set server.network-config.network-listeners.network-listener.http-listener-1.port="$PORT"
"$GF_HOME/asadmin" deploy --force --contextroot speakout /opt/speakout.war
"$GF_HOME/asadmin" stop-domain domain1

exec "$GF_HOME/asadmin" start-domain --verbose domain1

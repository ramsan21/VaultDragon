#!/bin/ksh
APP_HOME=/prd/starss/pgp-rest

echo "Running pgpUtility"

# Ensure JAVA_HOME is set correctly, replace this path with your actual JAVA_HOME if it's not set in the environment
JAVA_HOME={{ java_home }}
LOGS_DIR={{ logs_dir }}

# Running Java application with the main class specified
nohup $JAVA_HOME/bin/java -cp $APP_HOME/lib/pgp-rest.jar com.scb.dcda.ss.pgp.util.pgpUtility $* > $LOGS_DIR/pgpUtility.log 2>&1 &

echo "pgpUtility is running"



    #!/bin/ksh

APP_HOME=/prd/starss/pgp-rest

echo "Running pgpUtility"

{{ java_home }}/bin/java -classpath $APP_HOME/lib/pgp-rest.jar com.scb.dcda.ss.pgp.util.pgpUtility

echo "pgpUtility execution completed"

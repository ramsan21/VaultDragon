#!/bin/bash

# Start maintenance
echo "Starting maintenance..."
./prd/starss/maintenance.sh start

# Wait for 20 seconds
sleep 20

# Switch user to "nfast" and run installation command
echo "Switching user to 'nfast' and running install command..."
sudo -u nfast /opt/nfast/sbin/install

# Switch to 'jbossadm' user
echo "Switching user to 'jbossadm'..."
su - jbossadm <<'EOF'

# Shutdown instances
echo "Shutting down STARSEC instance..."
/jboss/tomcat/jws6/instances/SIT_01_CIB_STARSEC_HK/bin/shutdown.sh
echo "Shutting down STARSECADMIN instance..."
/jboss/tomcat/jws6/instances/SIT_01_CIB_STARSECADMIN_HK/bin/shutdown.sh

# Wait for 10 seconds
echo "Waiting for 10 seconds..."
sleep 10

# Start instances
echo "Starting STARSECADMIN instance..."
/jboss/tomcat/jws6/instances/SIT_01_CIB_STARSECADMIN_HK/bin/startup.sh
echo "Starting STARSEC instance..."
/jboss/tomcat/jws6/instances/SIT_01_CIB_STARSEC_HK/bin/startup.sh

EOF

# Stop maintenance
echo "Stopping maintenance..."
./prd/starss/maintenance.sh stop

echo "Script execution completed."
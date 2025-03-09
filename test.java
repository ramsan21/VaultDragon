#!/bin/bash

# Define the path to the properties file
PROPERTIES_FILE="/path/to/us-server.properties"

# Get the current hostname
HOSTNAME=$(hostname)

# Define mappings of hostnames to Hazelcast DNS addresses
case "$HOSTNAME" in
  "server1"|"serverA"|"node01")
    HAZELCAST_DNS="10.95.168.100,10.95.168.101,10.95.168.102"
    ;;
  "server2"|"serverB"|"node02")
    HAZELCAST_DNS="10.95.168.110,10.95.168.111,10.95.168.112"
    ;;
  "server3"|"serverC"|"node03")
    HAZELCAST_DNS="10.95.168.120,10.95.168.121,10.95.168.122"
    ;;
  *)
    echo "Hostname not recognized. Keeping the existing Hazelcast DNS address."
    exit 1
    ;;
esac

# Replace the existing hazelcast.dns.address in the properties file
sed -i "s|^hazelcast.dns.address=.*|hazelcast.dns.address=$HAZELCAST_DNS|" "$PROPERTIES_FILE"

# Verify the update
echo "Updated hazelcast.dns.address in $PROPERTIES_FILE"
grep "hazelcast.dns.address" "$PROPERTIES_FILE"
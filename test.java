#!/bin/bash

# Define the directory where the log files are stored
LOG_DIR="/prd/starss/pgp-rest/logs"

# Set the desired permissions (e.g., rwxr-xr-x for all files)
DESIRED_PERMISSIONS="755"

# Iterate over all the log files in the directory (both regular and gzipped)
for log_file in "$LOG_DIR"/*; do
    if [ -f "$log_file" ]; then
        echo "Adjusting permissions for $log_file"
        chmod $DESIRED_PERMISSIONS "$log_file"
    fi
done

echo "Log file permissions adjusted."
#!/bin/bash

# Set the directory path
directory="/path/to/your/folder"

# Get today's date in YYYY-MM-DD format
today=$(date +%F)

# Find matching files modified today and get the latest 6
file_count=$(find "$directory" -maxdepth 1 -type f -name '*_SuspendAlertReport*' \
  -newermt "$today" ! -newermt "$today +1 day" \
  -printf '%T@ %p\n' | sort -nr | head -6 | wc -l)

# Output the count
echo "$file_count"
#!/bin/bash

# Directory to search
directory="/path/to/your/folder"

# Today's date in YYYY-MM-DD format
today=$(date +%F)

# Find latest 6 matching files modified today and count them
file_count=$(find "$directory" -maxdepth 1 -type f -name "*_SuspendAlertReport*" \
    -newermt "$today" ! -newermt "$today +1 day" -printf '%T@ %p\n' \
    | sort -nr \
    | head -6 \
    | wc -l)

# Output the count
echo "$file_count"
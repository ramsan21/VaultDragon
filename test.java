#!/bin/bash

# Directory to check
directory="/path/to/your/folder"

# Get today's date in YYYY-MM-DD format
today=$(date +%F)

# Find latest 6 files, modified today
file_count=$(find "$directory" -maxdepth 1 -type f -newermt "$today" ! -newermt "$today +1 day" -printf '%T@ %p\n' \
             | sort -nr \
             | head -6 \
             | wc -l)

# Print count
echo "$file_count"
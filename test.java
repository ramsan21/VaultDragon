#!/bin/bash

# Directory to search
directory="/path/to/your/folder"

# Today's date
today=$(date +%F)

# Combined find for multiple patterns
file_count=$(find "$directory" -maxdepth 1 -type f \( \
    -name '*_SuspendAlertReport*' -o \
    -name '*_SuspendUserReport*' -o \
    -name '*_SuspendSummary*' \
    \) -newermt "$today" ! -newermt "$today +1 day" \
    -printf '%T@ %p\n' | sort -nr | head -6 | wc -l)

# Output the result
echo "$file_count"
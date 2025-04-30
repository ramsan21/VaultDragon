#!/bin/bash

filename="suspend_user.log"

# Check if the file exists
if [ -f "$filename" ]; then
    # Get the file's creation/modification date in YYYY-MM-DD format
    file_date=$(date -r "$filename" +%F)
    today=$(date +%F)

    if [ "$file_date" = "$today" ]; then
        echo "File $filename was created/modified today."

        # 4 grep checks (adjust patterns as needed)
        echo "Grep 1 (pattern: 'ERROR'):"
        grep "ERROR" "$filename"

        echo "Grep 2 (pattern: 'WARN'):"
        grep "WARN" "$filename"

        echo "Grep 3 (pattern: 'Suspended'):"
        grep "Suspended" "$filename"

        echo "Grep 4 (pattern: 'User'):"
        grep "User" "$filename"
    else
        echo "File $filename was NOT created/modified today. It was modified on $file_date."
    fi
else
    echo "File $filename does not exist."
fi
#!/bin/bash

# --- Part 1: Check suspend_user.log ---
log_file="suspend_user.log"
today=$(date +%F)

if [ -f "$log_file" ]; then
    log_date=$(date -r "$log_file" +%F)

    if [ "$log_date" = "$today" ]; then
        echo "File $log_file was modified today."

        echo "Grep 1 (pattern: 'ERROR'):"
        grep "ERROR" "$log_file"

        echo "Grep 2 (pattern: 'WARN'):"
        grep "WARN" "$log_file"

        echo "Grep 3 (pattern: 'Suspended'):"
        grep "Suspended" "$log_file"

        echo "Grep 4 (pattern: 'User'):"
        grep "User" "$log_file"
    else
        echo "File $log_file was NOT modified today. Last modified: $log_date"
    fi
else
    echo "File $log_file does not exist."
fi

# --- Part 2: Check last 6 SuspendUserReprt*.csv files ---
echo ""
echo "Checking last 6 SuspendUserReprt*.csv files..."

csv_files_found=false

# Get last 6 modified files
csv_files=$(ls -t SuspendUserReprt*.csv 2>/dev/null | head -n 6)

if [ -z "$csv_files" ]; then
    echo "No SuspendUserReprt*.csv files found."
else
    for file in $csv_files; do
        file_date=$(date -r "$file" +%F)
        file_size_kb=$(du -k "$file" | cut -f1)

        if [ "$file_date" = "$today" ] && [ "$file_size_kb" -gt 216 ]; then
            echo "File $file was modified today and is > 216 KB ($file_size_kb KB)."
            csv_files_found=true
        fi
    done

    if [ "$csv_files_found" = false ]; then
        echo "None of the last 6 files were modified today and > 216 KB."
    fi
fi
#!/bin/bash

# Check if the CSV file path is provided
if [ -z "$1" ]; then
    echo "Usage: $0 path/to/csvfile"
    exit 1
fi

# Get the CSV file path from the first argument
CSV_FILE="$1"

# Set the URL
URL="http://example.com/api"  # Replace with your URL

# Output log file
LOG_FILE="curl_logs.txt"

# Ensure the log file is empty before starting
> $LOG_FILE

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "jq is not installed. Please install jq to proceed."
    exit 1
fi

# Read the CSV file line by line
while IFS=, read -r appid groupid userid; do
    # Build the JSON payload
    jsonPayload=$(jq -n --arg appid "$appid" --arg groupid "$groupid" --arg userid "$userid" '{appid: $appid, groupid: $groupid, userid: $userid}')
    
    # Execute the curl command and capture the response
    response=$(curl -s -X POST -H "Content-Type: application/json" -d "$jsonPayload" "$URL")
    
    # Check if the curl command was successful
    if [ $? -ne 0 ]; then
        echo "Curl command failed for $appid, $groupid, $userid" >> $LOG_FILE
    fi
    
    # Log the results to the log file
    echo "$appid,$groupid,$userid,$response" >> $LOG_FILE
    
    # Optional: Print progress to the console
    echo "Processed appid: $appid, groupid: $groupid, userid: $userid"
done < "$CSV_FILE"

echo "All requests have been processed. Logs are stored in $LOG_FILE."
    

# Set the URL
URL="http://example.com/api"  # Replace with your URL

# Output log file
LOG_FILE="curl_logs.txt"

# Ensure the log file is empty before starting
> $LOG_FILE

# Read the CSV file line by line
while IFS=, read -r appid groupid userid; do
    # Build the JSON payload
    jsonPayload=$(jq -n --arg appid "$appid" --arg groupid "$groupid" --arg userid "$userid" '{appid: $appid, groupid: $groupid, userid: $userid}')
    
    # Execute the curl command and capture the response
    response=$(curl -s -X POST -H "Content-Type: application/json" -d "$jsonPayload" "$URL")
    
    # Check if the curl command was successful
    if [ $? -ne 0 ]; then
        echo "Curl command failed for $appid, $groupid, $userid" >> $LOG_FILE
    fi
    
    # Log the results to the log file
    echo "$appid,$groupid,$userid,$response" >> $LOG_FILE
    
    # Optional: Print progress to the console
    echo "Processed appid: $appid, groupid: $groupid, userid: $userid"
done < data.csv

echo "All requests have been processed. Logs are stored in $LOG_FILE."

  


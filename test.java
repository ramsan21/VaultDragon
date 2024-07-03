#!/bin/bash

# Check if the CSV file is provided as an argument
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <input_csv_file>"
    exit 1
fi

input_file="$1"
log_file="output.log"

# Ensure the log file is empty
> "$log_file"

echo "Processing file: $input_file"

# Read the CSV file line by line
while IFS=, read -r appId groupId userId rest; do
    # Skip empty lines
    if [[ -z "$appId" || -z "$groupId" || -z "$userId" ]]; then
        continue
    fi

    echo "Processing record: appId=$appId, groupId=$groupId, userId=$userId"

    # Prepare the JSON payload with the current appId, groupId, and userId
    json_payload=$(cat <<EOF
{
    "request": {
        "adminuser": {
            "appId"   : "IDC",
            "groupId" : "ADMINGROUP",
            "userId": "ADMINUSER5",
            "password": {"password":"04a1880d4acfa50439499c68f0337159", "type": 13}
        },
        "users": [
            {
                "appId": "$appId",
                "groupId": "$groupId",
                "userId": "$userId"
            }
        ]
    }
}
EOF
)

    # Execute the curl command with the generated JSON payload
    response=$(curl --location --request POST 'https://cibuaasuat.global.standardchartered.com:543/uaasadmv2/adminservices/DeleteUser' \
        --header 'Content-Type: application/json' \
        --data-raw "$json_payload" \
        --write-out "HTTPSTATUS:%{http_code}" \
        --silent \
        --output /dev/null)

    # Extract the HTTP status code from the response
    http_status=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')

    # Check if the response contains an error
    if [ "$http_status" -ne 200 ]; then
        echo "Error processing record: appId=$appId, groupId=$groupId, userId=$userId, HTTP Status=$http_status" | tee -a "$log_file"
    else
        # Get the response body
        response_body=$(curl --location --request POST 'https://cibuaasuat.global.standardchartered.com:543/uaasadmv2/adminservices/DeleteUser' \
            --header 'Content-Type: application/json' \
            --data-raw "$json_payload" \
            --silent)

        # Log the output
        echo "$appId, $groupId, $userId, $response_body" >> "$log_file"
    fi

done < "$input_file"

echo "Processing complete. Logs are saved in $log_file"

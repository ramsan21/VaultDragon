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

# Initialize arrays to store every 10 records
appIds=()
groupIds=()
userIds=()

# Initialize counters
record_count=0
batch_size=10

# Read the CSV file line by line
while IFS=, read -r appId groupId userId rest; do
    # Skip empty lines
    if [[ -z "$appId" || -z "$groupId" || -z "$userId" ]]; then
        continue
    fi

    # Store the values in arrays
    appIds+=("$appId")
    groupIds+=("$groupId")
    userIds+=("$userId")

    # Increment the record count
    record_count=$((record_count + 1))

    # Process every 10 records
    if (( record_count % batch_size == 0 )); then
        echo "Processing batch of $batch_size records"

        # Assign values to variables
        for i in $(seq 1 $batch_size); do
            eval "appId$i=${appIds[$((i-1))]}"
            eval "groupId$i=${groupIds[$((i-1))]}"
            eval "userId$i=${userIds[$((i-1))]}"
        done

        # Print variables (for debugging purposes)
        for i in $(seq 1 $batch_size); do
            eval "echo appId$i=\$appId$i, groupId$i=\$groupId$i, userId$i=\$userId$i"
        done

        # Prepare the JSON payload with the current batch
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
EOF
)
        for i in $(seq 1 $batch_size); do
            eval "appId=\$appId$i"
            eval "groupId=\$groupId$i"
            eval "userId=\$userId$i"
            json_payload+=$(cat <<EOF
            {
                "appId": "$appId",
                "groupId": "$groupId",
                "userId": "$userId"
            }$(if [ $i -lt $batch_size ]; then echo ","; fi)
EOF
)
        done
        json_payload+=$(cat <<EOF
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
            echo "Error processing batch: HTTP Status=$http_status" | tee -a "$log_file"
        else
            # Get the response body
            response_body=$(curl --location --request POST 'https://cibuaasuat.global.standardchartered.com:543/uaasadmv2/adminservices/DeleteUser' \
                --header 'Content-Type: application/json' \
                --data-raw "$json_payload" \
                --silent)

            # Log the output
            for i in $(seq 1 $batch_size); do
                eval "appId=\$appId$i"
                eval "groupId=\$groupId$i"
                eval "userId=\$userId$i"
                echo "$appId, $groupId, $userId, $response_body" >> "$log_file"
            done
        fi

        # Clear arrays for the next batch
        appIds=()
        groupIds=()
        userIds=()
    fi

done < "$input_file"

# Process any remaining records if the total number is not a multiple of the batch size
if (( record_count % batch_size != 0 )); then
    remaining_count=$((record_count % batch_size))
    echo "Processing remaining $remaining_count records"

    # Assign values to variables
    for i in $(seq 1 $remaining_count); do
        eval "appId$i=${appIds[$((i-1))]}"
        eval "groupId$i=${groupIds[$((i-1))]}"
        eval "userId$i=${userIds[$((i-1))]}"
    done

    # Print variables (for debugging purposes)
    for i in $(seq 1 $remaining_count); do
        eval "echo appId$i=\$appId$i, groupId$i=\$groupId$i, userId$i=\$userId$i"
    done

    # Prepare the JSON payload with the current batch
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
EOF
)
    for i in $(seq 1 $remaining_count); do
        eval "appId=\$appId$i"
        eval "groupId=\$groupId$i"
        eval "userId=\$userId$i"
        json_payload+=$(cat <<EOF
            {
                "appId": "$appId",
                "groupId": "$groupId",
                "userId": "$userId"
            }$(if [ $i -lt $remaining_count ]; then echo ","; fi)
EOF
)
    done
    json_payload+=$(cat <<EOF
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
        echo "Error processing batch: HTTP Status=$http_status" | tee -a "$log_file"
    else
        # Get the response body
        response_body=$(curl --location --request POST 'https://cibuaasuat.global.standardchartered.com:543/uaasadmv2/adminservices/DeleteUser' \
            --header 'Content-Type: application/json' \
            --data-raw "$json_payload" \
            --silent)

        # Log the output
        for i in $(seq 1 $remaining_count); do
            eval "appId=\$appId$i"
            eval "groupId=\$groupId$i"
            eval "userId=\$userId$i"
            echo "$appId, $groupId, $userId, $response_body" >> "$log_file"
        done
    fi
fi

echo "Processing complete. Logs are saved in $log_file"

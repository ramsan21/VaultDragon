#!/bin/bash

# Check if a filepath was provided
if [ -z "$1" ]; then
    echo "Usage: $0 <filepath>"
    exit 1
fi

FILEPATH="$1"

# Check if file exists
if [ ! -f "$FILEPATH" ]; then
    echo "Error: File does not exist."
    exit 2
fi

# Update <URL> and <JWTTOKEN> with actual values
curl --location --request POST '<URL>' \
--header 'X-Service-JWT: <JWTTOKEN>' \
--form "file=@\"$FILEPATH\""

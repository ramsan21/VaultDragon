#!/bin/bash

# Check if an output filename was provided without extension
if [ -z "$1" ]; then
    echo "Usage: $0 <output_filename_base>"
    exit 1
fi

# Hardcoded URL of the public key
KEY_URL="http://example.com/somefile.bin"
OUTPUT_FILE="$1.asc"

# Use curl to download the public key and overwrite if exists
curl -o "$OUTPUT_FILE" "$KEY_URL"

# Check if the download was successful
if [ -f "$OUTPUT_FILE" ]; then
    # Retrieve any existing key associated with the file
    KEY_ID=$(gpg --with-fingerprint "$OUTPUT_FILE" | awk '/Key fingerprint = / {print $5}')
    
    if [ -n "$KEY_ID" ]; then
        # Delete the key if it exists
        gpg --batch --yes --delete-keys "$KEY_ID"
    fi
    
    # Import the public key using GPG
    gpg --import "$OUTPUT_FILE"

    # Optionally, inform the user that the process was successful
    echo "Public key imported successfully. File is saved as $OUTPUT_FILE"
else
    echo "Failed to download the public key."
    exit 1
fi

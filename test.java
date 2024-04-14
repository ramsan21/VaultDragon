#!/bin/bash

# Check if the required arguments are provided
if [ $# -ne 2 ]; then
    echo "Usage: $0 localuser recipient"
    exit 1
fi

localuser="$1@example.com"
recipient="$2"

# Paths to the files involved
input_file="/data/signenc_in_gpg.txt"
encrypted_file="/data/signenc_out_pgp.txt"
decrypted_file="/data/decverify_out_api.txt"

# Sign and encrypt the file
gpg --local-user "$localuser" --sign --encrypt --armor --recipient "$recipient" --output "$encrypted_file" "$input_file"

# Check if GPG operation was successful
if [ $? -ne 0 ]; then
    echo "GPG encryption failed."
    exit 2
fi

# Replace '<url>' and '<token>' with actual URL and JWT token values
curl --location --request POST '<url>' \
--header 'X-Service-JWT: <token>' \
--header 'Content-Type: application/json' \
--data-raw "{
    \"inputFile\":\"$encrypted_file\",
    \"outputFile\":\"$decrypted_file\",
    \"groupId\":\"groupId\",
    \"keyId\":\"$recipient\",
    \"format\":\"format\"
}"

# Check if curl operation was successful
if [ $? -ne 0 ]; then
    echo "API call failed."
    exit 3
fi

# Verify that the content of the original and decrypted files are the same
if cmp -s "$input_file" "$decrypted_file"; then
    echo "Success: File contents are identical."
else
    echo "Failure: File contents differ."
fi

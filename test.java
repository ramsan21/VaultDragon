#!/bin/bash

# Check if a name was provided
if [ -z "$1" ]; then
    echo "Usage: $0 <name>"
    exit 1
fi

NAME=$1
PASSPHRASE="P@ssw0rd"
KEYNAME="$NAME@example.com"  # Modify the domain as needed
OUTPUT_FILE="$NAME.asc"

# Check if a key already exists
EXISTING_KEY=$(gpg --list-keys "$KEYNAME" | grep uid)
if [ -n "$EXISTING_KEY" ]; then
    # Extract key ID for deletion
    KEY_ID=$(gpg --list-keys --with-colons "$KEYNAME" | awk -F: '/^pub/{print $5}')
    echo "Deleting existing key $KEY_ID"
    gpg --batch --yes --delete-secret-and-public-key "$KEY_ID"
fi

# Create a new GPG key pair
cat <<EOF | gpg --batch --generate-key
    Key-Type: RSA
    Key-Length: 2048
    Subkey-Type: RSA
    Subkey-Length: 2048
    Name-Real: $NAME
    Name-Comment: Automatically generated
    Name-Email: $KEYNAME
    Expire-Date: 0
    Passphrase: $PASSPHRASE
EOF

# Export the public key
gpg --armor --export "$KEYNAME" > "$OUTPUT_FILE"
echo "Public key exported to $OUTPUT_FILE"

# Example curl command to upload the file
# curl -F "file=@$OUTPUT_FILE" http://example.com/upload
# For demonstration, we'll just display the file path
echo "The file path of the public key: $(realpath "$OUTPUT_FILE")"

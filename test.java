#!/bin/bash

# Usage: ./script.sh file1.txt file2.txt

# Check if two files were provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 file1 file2"
    exit 1
fi

# Check if files exist
if [ ! -e "$1" ]; then
    echo "Error: $1 does not exist."
    exit 1
fi

if [ ! -e "$2" ]; then
    echo "Error: $2 does not exist."
    exit 1
fi

# Compare files
if cmp -s "$1" "$2"; then
    echo "Files are the same."
else
    echo "Files are different."
fi

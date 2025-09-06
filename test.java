#!/bin/bash

# Example: set ENB variable
ENB=$1   # or hardcode it, e.g., ENB="STG"

if [ "$ENB" = "STG" ] || [ "$ENB" = "production" ]; then
    echo "Environment is $ENB. Performing action..."
    # put your action here
    # e.g., deploy, run a command, etc.
    # ./deploy.sh
else
    echo "Environment is $ENB. No action taken."
fi
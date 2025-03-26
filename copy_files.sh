#!/bin/bash

# Check if hex file argument is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <hex_file_name>"
    echo "Example: $0 program.hex"
    echo "This script copies the specified hex file from ./hex/instructions/ and"
    echo "SmTopDe2115.v to the QuartusProjects/sm directory."
    exit 1
fi

# Define source and destination paths
HEX_FILE="$1"
SOURCE_HEX="./hex/instructions/$HEX_FILE"
SOURCE_VERILOG="./generated/SmTopDe2115.v"
DEST_HEX="/home/arijus/QuartusProjects/sm/bootkernel.hex"
DEST_VERILOG="/home/arijus/QuartusProjects/sm/SmTopDe2115.v"

# Function to check if a file exists
check_file_exists() {
    if [ ! -f "$1" ]; then
        echo "Error: Source file $1 does not exist."
        exit 1
    fi
}

# Function to create directory if it doesn't exist
create_directory_if_needed() {
    if [ ! -d "$1" ]; then
        echo "Creating directory: $1"
        mkdir -p "$1"
        if [ $? -ne 0 ]; then
            echo "Error: Failed to create directory $1"
            exit 1
        fi
    fi
}

# Check if source files exist
echo "Checking source files..."
check_file_exists "$SOURCE_HEX"
check_file_exists "$SOURCE_VERILOG"

# Create destination directories if they don't exist
echo "Checking destination directories..."
create_directory_if_needed "$(dirname "$DEST_HEX")"
create_directory_if_needed "$(dirname "$DEST_VERILOG")"

# Copy hex file
echo "Copying $SOURCE_HEX to $DEST_HEX..."
cp "$SOURCE_HEX" "$DEST_HEX"
if [ $? -ne 0 ]; then
    echo "Error: Failed to copy $SOURCE_HEX to $DEST_HEX"
    exit 1
fi

# Copy Verilog file
echo "Copying $SOURCE_VERILOG to $DEST_VERILOG..."
cp "$SOURCE_VERILOG" "$DEST_VERILOG"
if [ $? -ne 0 ]; then
    echo "Error: Failed to copy $SOURCE_VERILOG to $DEST_VERILOG"
    exit 1
fi

echo "Success! All files copied successfully."
echo "  - $SOURCE_HEX → $DEST_HEX"
echo "  - $SOURCE_VERILOG → $DEST_VERILOG"

exit 0


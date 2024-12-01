#!/usr/bin/env bash
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# Extract file name of the input file
input_path="$1"
file_name=$(basename "$input_path")

jfr_file="$script_dir/app/jfr/$file_name.jfr"

mkdir -p "$script_dir/app/jfr"

# Use the extracted file name for the JFR file name
java -XX:+UnlockDiagnosticVMOptions \
     -XX:+DebugNonSafepoints \
     -XX:+PrintGCDetails \
     -XX:StartFlightRecording=duration=60s,filename="$jfr_file" \
     -jar "$script_dir/app/build/libs/app.jar" "$@"

# Check if the environment variable to open VisualVM is set
if [ -n "$OPEN_JFR_WITH_VISUALVM" ]; then
    # Open the JFR file with VisualVM (assuming it's installed and available in your PATH)
    visualvm --openfile "$jfr_file" > /dev/null 2>&1 &
fi
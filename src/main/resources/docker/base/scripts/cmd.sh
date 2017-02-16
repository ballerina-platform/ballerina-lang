#!/usr/bin/env bash
function collectArgs() {
    if [ ! -z "$FILE_MODE" ] && [ "$FILE_MODE" = "true" ]; then
        # Running single Ballerina files
        arg_str=$(find /ballerina/files -type f -name "*.bal")
    elif [ ! -z "$SVC_MODE" ] && [ "$SVC_MODE" = "true" ]; then
        # Running Ballerina service packages
        arg_str=$(find /ballerina/files -type f -name "*.bsz")
    else
        # Running Ballerina main packages
        arg_str=$(find /ballerina/files -type f -name "*.bmz")
    fi
}

collectArgs

if [ ! -z "$SVC_MODE" ] && [ "$SVC_MODE" = "true" ]; then
  cmd="ballerina service"
else
  cmd="ballerina run"
fi

echo "Running: ${cmd} ${arg_str}"
bash $cmd $arg_str

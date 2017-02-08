#!/usr/bin/env bash
arg_str=$(find /ballerina/files -type f -name "*.bal")

if [ ! -z "$SVC_MODE" ] && [ "$SVC_MODE" = "true" ]; then
  cmd="ballerina service"
else
  cmd="ballerina run"
fi

bash $cmd $arg_str

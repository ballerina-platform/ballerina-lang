#!/usr/bin/env bash
arg_str=$(find /ballerina/files -type f -name "*.bal")

if [ ! -z "$SVC_MODE" ] && [ "$SVC_MODE" = "true" ]; then
  cmd="ballerinaserver"
else
  cmd="ballerina"
fi

bash $cmd $arg_str

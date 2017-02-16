#!/usr/bin/env bash

# Gather argument string from test Ballerina services
test_services=$(find $bal_files_home -type f -name "*.bal")
arg_str="${test_services}"

# Gather sample string from shipped Ballerina samples. Only picks up files with "Service" in the name
samples_str=$(find "/maven/ballerina-${project_version}/samples" -type f -name "*Service*.bal")

if [ "$load_samples" == "true" ]; then
    arg_str="${test_services} ${samples_str}"
fi

# Run ballerinaServer with the argument string
/maven/ballerina-$project_version/bin/ballerina service $arg_str
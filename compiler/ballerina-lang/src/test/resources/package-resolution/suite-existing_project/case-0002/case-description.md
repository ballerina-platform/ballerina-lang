# Adding of a new import which is already there as a transitive in the graph with an old version

1. User has `ballerina/http` in the project where `ballerina/http -> ballerina/cache:1.2.1, ballerina/io:1.0.1`
2. Current distribution contains `ballerina/cache` versions: `1.2.1`, `1.4.0`
3. Central repo contains `ballerina/io` versions: `1.0.1`, `1.0.2`, `1.1.0`
4. User adds the import `ballerina/cache` in the project

## Expected behavior

### Sticky == true
Dependency graph should be updated to have `ballerina/cache:1.4.0`, no changes to `ballerina/io:1.0.1`

### Sticky == false
Dependency graph should be updated to have `ballerina/cache:1.4.0`, `ballerina/io:1.0.2`


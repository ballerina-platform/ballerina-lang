# Remove existing import which also is a dependency of a newer patch version of another import

1. User has `ballerina/auth:2.0.0` and `ballerina/cache:1.2.1` in the project where, `ballerina/auth:2.0.0 -> ballerina/io:1.0.1`
3. Current distribution contains `ballerina/cache` versions: `1.2.1`, `1.3.1`, `1.3.2`, `1.4.0`
3. Central repo contains `ballerina/io` versions: `1.0.1`, `1.0.2`, `1.1.0` and
`ballerina/auth` versions: `2.0.0` and `2.0.1` where, `ballerina/auth:2.0.0 -> ballerina/cache:1.3.1`
4. User removes the import `ballerina/cache` from the project

## Expected behavior

### Sticky == true
No changes to the graph. 
`ballerina/cache` should be removed from the dependency graph

### Sticky == false
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.0.2` and `ballerina/cache:1.3.2` should
be newly added as a dependency of `ballerina/auth`


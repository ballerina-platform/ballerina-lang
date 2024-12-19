# Remove existing import which is also a transitive dependency from another import

1. User has `ballerina/http:1.3.1` and `ballerina/cache:1.4.0` in the project where the static dependency graph of 
`ballerina/http` is `ballerina/http:1.3.1 -> ballerina/cache:1.2.1, ballerina/io:1.0.1`
2. Current distribution contains `ballerina/cache` versions: `1.2.1`, `1.4.0`
3. Central repo contains `ballerina/io` versions: `1.0.1`, `1.0.2`, `1.1.0`
4. User removes the import `ballerina/cache` from the project

## Expected behavior

### Update policy == SOFT
Dependency graph should be updated to have `ballerina/http:1.4.0`, `ballerina/io:1.2.0`, `samjs/foo:1.3.0`.
### Update policy == MEDIUM
Dependency graph should be updated to have `ballerina/io:1.0.2`.
### Update policy == HARD
No changes to the dependency graph.
### Update policy == LOCKED
No changes to the dependency graph.

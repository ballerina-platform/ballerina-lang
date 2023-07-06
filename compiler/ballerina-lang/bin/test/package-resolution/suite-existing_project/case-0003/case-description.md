# Remove existing import which is also a transitive dependency from another import

1. User has `ballerina/http:1.3.1` and `ballerina/cache:1.4.0` in the project where the static dependency graph of 
`ballerina/http` is `ballerina/http:1.3.1 -> ballerina/cache:1.2.1, ballerina/io:1.0.1`
2. Current distribution contains `ballerina/cache` versions: `1.2.1`, `1.4.0`
3. Central repo contains `ballerina/io` versions: `1.0.1`, `1.0.2`, `1.1.0`
4. User removes the import `ballerina/cache` from the project

## Expected behavior

### Sticky == true
No changes to the graph. 
The latest version of `ballerina/cache` `1.4.0 ` is already locked in Dependencies.toml, no changes to `ballerina/io:1.0.1`

### Sticky == false
Dependency graph should be updated to have `ballerina/io:1.0.2`


# Add a new hierarchical import which has a possible package name in the Dependencies.toml

1. User has `ballerina/auth:2.0.0` and the submodule `protobuf.types.empty` of `ballerina/protobuf:0.6.0` in the project where, `ballerina/auth:2.0.0 -> ballerina/io:1.0.1`
2. Current distribution contains `ballerina/cache` versions: `1.2.1`, `1.3.1`, `1.3.2`, `1.4.0`
3. Central repo contains `ballerina/io` versions: `1.0.1`, `1.0.2`, `1.1.0`, `1.2.0` and
   `ballerina/auth` versions: `2.0.0` and `2.0.1` where, `ballerina/auth:2.0.1 -> ballerina/cache:1.3.1`
4. `ballerina/protobuf` has versions `0.6.0`, `0.7.0`, `1.6.0`, and `1.7.0`
5. User adds a new import `ballerina/protobuf.types.timestamp` which possibly can be a submodule of `ballerina/protobuf`

## Expected behavior

### Update policy == SOFT
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.2.0`. `ballerina/cache:1.4.0`, 
`ballerina/protobuf.types.timestamp:1.0.0` should be added as newly added dependencies.
### Update policy == MEDIUM
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.0.2`. `ballerina/cache:1.3.2`,
`ballerina/protobuf.types.timestamp:1.0.0` should be added as newly added dependencies.
### Update policy == HARD
Dependency graph should be updated to add `ballerina/protobuf.types.timestamp:1.0.0`
### Update policy == LOCKED
Build failure since import addition is not allowed in the LOCKED update policy.

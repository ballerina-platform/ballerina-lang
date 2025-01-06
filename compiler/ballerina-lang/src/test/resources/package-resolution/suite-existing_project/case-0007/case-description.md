# Package contains hierarchical imports

1. User has `ballerina/auth:2.0.0` and the submodule `protobuf.types.empty` of `ballerina/protobuf:0.6.0` in the project where, `ballerina/auth:2.0.0 -> ballerina/io:1.0.1`
2. Current distribution contains `ballerina/cache` versions: `1.2.1`, `1.3.1`, `1.3.2`, `1.4.0`
3. Central repo contains `ballerina/io` versions: `1.0.1`, `1.0.2`, `1.1.0`, `1.2.0` and
   `ballerina/auth` versions: `2.0.0` and `2.0.1` where, `ballerina/auth:2.0.1 -> ballerina/cache:1.3.1`
4. `ballerina/protobuf` has versions `0.6.0`, `0.7.0`, `1.6.0`, and `1.7.0`

## Expected behavior

### Update policy == SOFT
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.2.0` and `ballerina/cache:1.4.0` should
be newly added as a dependency of `ballerina/auth`
### Update policy == MEDIUM
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.0.2` and `ballerina/cache:1.3.2` should
be newly added as a dependency of `ballerina/auth`
### Update policy == HARD
No changes to the graph. `ballerina/protobuf:0.7.0` is considered a breaking change from `ballerina/protobuf:0.6.0`
### Update policy == LOCKED
No changes to the graph.

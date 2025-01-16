# Package uses a module available in the newer minor version of an existing dependency

1. User has `ballerina/auth:2.0.0` and `ballerina/protobuf:0.6.0` in the project where, `ballerina/auth:2.0.0 -> ballerina/io:1.0.1`
2. `ballerina/protobuf` has versions `0.6.0`, `0.7.0`, `1.6.0`, and `1.7.0`
3. User adds a new import `ballerina/protobuf.types.duration` which possibly can be a submodule of `ballerina/protobuf`

## Expected behavior

### Update policy == SOFT
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.2.0`. `ballerina/cache:1.4.0`,
`ballerina/protobuf:1.7.0`.
### Update policy == MEDIUM
Dependency graph should be updated to have `ballerina/auth:2.0.1`, `ballerina/io:1.0.2`. `ballerina/cache:1.3.2`,
`ballerina/protobuf:1.7.0`.
### Update policy == HARD
Dependency graph should be updated to have `ballerina/io:1.2.0`.
### Update policy == LOCKED
Build failure since import addition is not allowed in the LOCKED update policy.

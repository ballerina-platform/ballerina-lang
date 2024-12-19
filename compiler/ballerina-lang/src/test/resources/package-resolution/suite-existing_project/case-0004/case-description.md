# Package contains a built-in transitive dependency with a no-zero version

1. User's package has `ballerinai/transaction:1.0.1` as a transitive dependency (built with slbeta2) 
and `ballerina/io:1.0.1` as a transitive dependency
2. Current distribution has `ballerinai/transaction:0.0.0`
3. A newer patch version `ballerina/io:1.0.2` has been released to central
4. User now builds the package using the current distribution

## Expected behavior

### Update policy == SOFT
Dependency graph should be updated to have `ballerina/http:1.4.0`, `samjs/foo:1.3.0`, `ballerina/io:1.2.0`, `ballerinai/transaction:0.0.0`, `ballerina/cache:1.4.0`.
### Update policy == MEDIUM
Dependency graph should be updated to have `ballerinai/transaction:0.0.0`, `ballerina/io:1.0.2`
### Update policy == HARD
Dependency graph should be updated to have `ballerinai/transaction:0.0.0`.
### Update policy == LOCKED
Dependency graph should be updated to have `ballerinai/transaction:0.0.0`.

# Package contains a built-in transitive dependency with a no-zero version

1. User's package has `ballerinai/transaction:1.0.15` as a transitive dependency (built with slbeta2) 
and `ballerina/io:1.0.1` as a transitive dependency
2. Current distribution has `ballerinai/transaction:0.0.0`
3. A newer patch version `ballerina/io:1.0.2` has been released to central
4. User now builds the package using the current distribution

## Expected behavior

### Sticky == true
Dependency graph should be updated to have `ballerinai/transaction:0.0.0`, no changes to `ballerina/io:1.0.1`
### Sticky == false
Dependency graph should be updated to have `ballerinai/transaction:0.0.0` and `ballerina/io:1.0.2`


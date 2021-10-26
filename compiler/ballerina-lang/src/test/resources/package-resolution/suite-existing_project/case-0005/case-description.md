# Package contains a built-in transitive dependency which has its dependencies changes in the current dist

1. User's package has `ballerinai/foo:0.1.0` as a transitive dependency (built with slbeta2) 
where, `ballerinai/foo:0.1.0 -> {}` (no dependencies)
 
 App dependency graph:
 ```dot
digraph "example1" {
    "myOrg/app:0.1.0" -> "myOrg/bazz:1.0.0"
    "myOrg/bazz:1.0.0" -> "myOrg/bar:1.3.1"
    "myOrg/bar:1.3.1" -> "ballerinai/foo:0.1.0"

    "myOrg/bar:1.3.1" [transitive = true]
    "ballerinai/foo:0.1.0" [transitive = true]
}
 ```
2. Current distribution has `ballerinai/foo:0.0.0` where, `ballerinai/foo:0.0.0 -> ballerina/cache:1.3.1`
3. Dist repo has `ballerina/cache` versions: `1.3.1`, `1.3.2`, `1.4.0`
4. Central repo has `myOrg/bar:1.3.1 -> ballerinai/foo:0.1.0`
3. User now builds the package using the current distribution

## Expected behavior

### Sticky == true
Dependency graph should be updated to have `ballerinai/transaction:0.0.0` and `ballerina/cache:1.3.2`

### Sticky == false
Same behavior as sticky ==true


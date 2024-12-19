# A new patch and minor version of a transitive has been released to central

1. User's package has `ballerina/io:1.0.1` as a transitive dependency. 
2. A newer patch version `ballerina/io:1.0.2` has been released to central. 
3. User now builds the package

## Expected behavior

### Update policy == SOFT
Dependency graph should be updated to have `ballerina/http:1.4.0`, `ballerina/io:1.2.0`, `ballerina/cache:1.4.0`, 
 and `samjs/foo:1.3.0`, 
### Update policy == MEDIUM
Dependency graph should be updated to have `ballerina/io:1.0.2`
### Update policy == HARD
No changes to Dependency graph
### Update policy == LOCKED
No changes to Dependency graph

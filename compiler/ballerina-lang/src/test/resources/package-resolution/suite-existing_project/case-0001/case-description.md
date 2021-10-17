# A new patch and minor version of a transitive has been released to central

1. User's package has `ballerina/io:1.0.1` as a transitive dependency. 
2. A newer patch version `ballerina/io:1.0.2` has been released to central. 
3. User now builds the package

## Expected behavior

### Sticky == true
No changes to Dependency graph
### Sticky == false
Dependency graph should be updated to have `ballerina/io:1.0.2`


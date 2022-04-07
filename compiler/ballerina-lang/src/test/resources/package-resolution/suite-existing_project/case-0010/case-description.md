# A newer pre-release version of a dependency has been released to central

1. User's package has `ballerina/io:1.0.1` as a transitive dependency. 
2. A newer pre-release versions `ballerina/io:1.3.0-beta.1` and `ballerina/io:2.0.0-beta.1` has been released to central. 
 `ballerina/io:1.2.0` also has been released to central.
3. User now builds the package

## Expected behavior

### Sticky == true
No changes to Dependency graph
### Sticky == false
Dependency graph should be updated to have `ballerina/io:1.2.0`


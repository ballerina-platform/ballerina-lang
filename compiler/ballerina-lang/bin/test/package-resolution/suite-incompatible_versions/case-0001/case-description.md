# User specifies an incompatible version of a direct dependency in the Ballerina.toml

1. User's package has `asmaj/foo:0.6.0` direct dependency and the version is locked in the Dependencies.toml. 
2. User specifies `asmaj/foo:0.6.0` with local repo. 
3. User now builds the package

## Expected behavior

### Sticky == true
Dependency graph should contain `asmaj/foo:0.6.0` but as an error node. 
### Sticky == false
Dependency graph should contain `asmaj/foo:0.6.0` but as an error node. 

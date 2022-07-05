# User specifies an incompatible version of a direct dependency in the Ballerina.toml - advanced

1. User's package has `asmaj/bar:1.2.4` as a direct dependency and the version is locked in the Dependencies.toml. 
2. User specifies `asmaj/bar:2.0.0` with local repo. 
3. User now builds the package

## Expected behavior

### Sticky == true
Dependency graph should contain `asmaj/bar:1.2.4` but as an error node. 
### Sticky == false
Dependency graph should contain `asmaj/bar:1.2.4` but as an error node. 


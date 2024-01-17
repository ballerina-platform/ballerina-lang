# User adds a new dependency which has a dependency that conflicts with an existing direct dependency

1. User's package has `asmaj/foo:0.7.0` as a direct dependency and the version is locked in the Dependencies.toml. 
2. User specifies `asmaj/bar:1.2.4` where  `asmaj/bar:1.2.4` depends on `asmaj/foo:0.6.0`
3. User now builds the package

## Expected behavior

### Sticky == true
Dependency graph should contain `asmaj/foo:0.7.0` but as an error node. 
### Sticky == false
Dependency graph should contain `asmaj/foo:0.7.0` but as an error node. 


# Package name is hierarchical, there are new versions in the central, and the older version is specified in Ballerina.toml and Dependencies.toml

1. User's package has `samjs/qux.foo:1.0.2` as a dependency in Dependencies.toml.
2. A newer version `samjs/qux.foo:1.0.5` has been released to central.
3. User specifies `samjs/qux.foo:1.0.2` in Ballerina.toml
4. User now builds the package

## Expected behavior

### Sticky == true
No changes to Dependency graph
### Sticky == false
Dependency graph should be updated to have `samjs/qux.foo:1.0.5`

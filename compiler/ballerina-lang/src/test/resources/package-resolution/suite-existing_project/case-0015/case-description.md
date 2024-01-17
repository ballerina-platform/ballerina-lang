# Transitive dependency is updated using Ballerina toml while not local

1. User's package has `samjs/http:1.0.0` as a dependency with `samjs/io:1.0.1` as a transitive
   dependency.
2. User specifies `samjs/io:1.1.0` in Ballerina.toml
3. User now builds the package

## Expected behavior

### Sticky == true
Dependency graph should be updated to have `samjs/io:1.1.0`

### Sticky == false
Dependency graph should be updated to have `samjs/io:1.1.0`

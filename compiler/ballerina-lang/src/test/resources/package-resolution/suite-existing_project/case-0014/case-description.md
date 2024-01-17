# Two dependencies are specified one of which in the Ballerina toml not from central.

1. User's package has `ballerina/io:1.0.1` and `samjs/io:1.0.1` as a dependency.
2. A newer version `ballerina/io:1.1.0` and `samjs/io:1.0.2` has been released to central.
   `ballerina/io:1.2.0` also has been released to central.
3. User specifies `ballerina/io:1.1.0` in Ballerina.toml
3. User now builds the package

## Expected behavior

### Sticky == true
Dependency graph should be updated to have `ballerina/io:1.1.0` and `samjs/io:1.0.1`

### Sticky == false
Dependency graph should be updated to have `ballerina/io:1.1.0` and `samjs/io:1.0.2`

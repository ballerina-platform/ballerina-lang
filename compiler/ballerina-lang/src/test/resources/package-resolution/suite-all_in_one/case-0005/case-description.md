# Fresh package with a dependency version specified in Ballerina.toml

The package does not have a Dependencies.toml file. The version of `samjs/c`
is specified in the Ballerina.toml as `0.4.5`, which is not the latest version
available in central (`0.4.6` exists). `samjs/c` is both a direct dependency
(imported by the package) and a transitive dependency (via `samjs/foo`).

## Expected behavior (see ballerina-spec#1247)

### HARD
`samjs/c` resolves to the exact user-specified version `0.4.5`, while all
other dependencies (including the transitives of `samjs/c`) resolve to the
latest compatible versions.

### MEDIUM
`samjs/c` resolves to the latest patch version `0.4.6`.

### SOFT
`samjs/c` resolves to the latest compatible version `0.4.6`.

# Build a package with a workspace dependency

1. User imports `samjs/toml:1.0.0`
2. `samjs/toml:1.0.1` is available in the Ballerina Central

## Expected behavior

### Sticky == true
`samjs/toml:1.0.0` from the workspace should be used.

### Sticky == false
`samjs/toml:1.0.0` from the workspace should be used.


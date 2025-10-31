# Build a package with both direct and transitive dependencies from the workspace

1. User imports `samjs/hr:1.0.0` which is in the workspace, 
2. `samjs/hr:1.0.0` has a dependency on `samjs/common:1.0.0` which is also in the workspace.

## Expected behavior

### Sticky == true
`samjs/hr:1.0.0` from the workspace should be used.
`samjs/common:1.0.0` from the workspace should be used.

### Sticky == false
`samjs/hr:1.0.0` from the workspace should be used.
`samjs/common:1.0.0` from the workspace should be used.


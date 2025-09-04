# Build a package with both direct and transitive dependencies from the workspace containing a Dependencies.toml

1. User imports `samjs/hr.leave` 
3. The Dependencies.toml has `samjs/hr.leave:1.0.0`
4. Workspace has `samjs/hr.leave:1.0.1` 
5. Central has `samjs/hr.leave:1.0.0`

## Expected behavior

### Sticky == true
`samjs/hr.leave:1.0.0` from the central should be used.

### Sticky == false
`samjs/hr.leave:1.0.1` from the workspace should be used.

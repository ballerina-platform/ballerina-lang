# Build a package with both direct and transitive dependencies from the workspace containing a Dependencies.toml

1. User imports `samjs/testutils` (scope = test)  and `ballerina/http` (scope = default)
4. Workspace has `samjs/hr.leave` 
5. Central has `ballerina/http`

## Expected behavior

### Sticky == true
`samjs/testutils:1.1.0` from the workspace should be used.
`ballerina/http:2.0.0` from the Central should be used.

### Sticky == false
`samjs/testutils:1.1.0` from the workspace should be used.
`ballerina/http:2.0.0` from the Central should be used.

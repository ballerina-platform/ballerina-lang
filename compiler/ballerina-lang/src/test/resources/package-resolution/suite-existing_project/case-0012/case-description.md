# A dependency has only pre-release versions released to central

1. User has imported `ballerinax/observe:1.0.0-alpha` in the app package 
2. Two pre-release versions `ballerinax/observe:1.0.0-alpha` and `ballerinax/observe:1.0.0-beta.1` has been released to central. 
3. User now builds the package

## Expected behavior

### Sticky == true
No changes to `ballerinax/observe:1.0.0-alpha` in the graph
### Sticky == false
Dependency graph should be updated to have `ballerinax/observe:1.0.0-beta.1`


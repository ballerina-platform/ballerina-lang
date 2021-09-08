This test suite contains cases that uses packages from the local repository. Here is the list of high-level groups.

#### Scenarios that use unpublished new packages
- Case 1: Ballerina program depends (default scope) on a local repository package that doesn't have any other dependencies.
- Case 2: Same as above, but with a dependencies.toml file
- Case 3: Ballerina program depends (test_only scope) on a local repository package that doesn't have any other dependencies.
- Case 4: Same as above, but with a dependencies.toml file
- Case 5: Local repository package that depends on a package in central
- Case 6: Same as above, but with a dependencies.toml file
- Case 7: Local repository package that depends on another local unpublished package
- Case 8: Same as above, but with a dependencies.toml file

#### Scenarios that use unpublished new versions of existing packages

#### Scenarios that use patched versions of existing packages
- Case 1) Patch a direct dependency
- Case 2) Patch a transitive dependency
- Case 3) Patch direct and transitive dependencies

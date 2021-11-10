This test suite contains cases that uses packages from the local repository. Here is the list of high-level groups.

#### Scenarios that use unpublished new packages
- Case 0001: Ballerina program depends (default scope) on a local repository package that doesn't have any other dependencies.
- Case 0002: Same as above, but with a dependencies.toml file
- Case 0003: Ballerina program depends (test_only scope) on a local repository package that doesn't have any other dependencies.
- Case 0004: Same as above, but with a dependencies.toml file
- Case 0005: Local repository package that depends on a package in central
- Case 0006: Same as above, but with a dependencies.toml file
- Case 0007: Local repository package that depends on another local unpublished package
- Case 0008: Same as above, but with a dependencies.toml file

#### Scenarios that use patched versions of existing packages
- Case 0101: Patch a direct dependency
- Case 0102: Patch a transitive dependency
- Case 0103: Patch direct and transitive dependencies

#### Scenarios that use unpublished new versions of existing packages

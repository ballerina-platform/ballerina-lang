This test suite contains cases that uses packages from the workspace repository. Here is the list of high-level groups.

#### Scenarios that use unpublished new packages
- Case 0001: Ballerina program depends (default scope) on a workspace repository package that doesn't have any other dependencies.
- Case 0002: Same as above, a higher compatible version of the package is available in the Ballerina Central repository.
- Case 0003: Ballerina program depends on a workspace repository package, which has a dependency on another workspace package.
- Case 0004: Same as case0001, but with a dependencies.toml file. The version in the workspace is higher than the one locked in the dependencies.toml.
- Case 0005: Ballerina program depends on a workspace repository package for tests
- Case 0006: Ballerina program depends on multiple repositories including a workspace dependency
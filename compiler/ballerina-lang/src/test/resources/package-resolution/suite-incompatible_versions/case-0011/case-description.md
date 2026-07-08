# Fresh project: Parent upgrade changes dependency structure with incompatible transitive dep versions

Same scenario as case-0009 but without Dependencies.toml (fresh project).

1. User's package depends on `testorg/pkgx`
2. Central has X:1.0.0 -> Y:1.1.0 -> Z:0.2.0
3. Central has X:1.1.0 -> W:1.2.0 -> Z:0.3.0
4. Z:0.2.0 and Z:0.3.0 are incompatible (different minor pre-1.0)

## Expected behavior

### All modes (HARD, MEDIUM, SOFT)
Since this is a fresh project, the latest version of pkgx (1.1.0) is resolved directly.
Dependency graph: X:1.1.0 -> W:1.2.0 -> Z:0.3.0. No conflict since Z:0.2.0 is never in the graph.

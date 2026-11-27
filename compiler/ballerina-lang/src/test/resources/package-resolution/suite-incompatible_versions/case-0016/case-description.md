# Rebuild with a Dependencies.toml whose locked versions drop a transient incompatible version

1. User's package depends on `testorg/wrapper:1.0.0` which was published with `testorg/connector:1.1.0`
2. `testorg/connector:1.1.0` depends on `testorg/datalib:0.8.0`
3. A previous build upgraded `testorg/connector` to `1.2.0`, which depends on `testorg/datalib:0.10.0`,
   and wrote that graph to Dependencies.toml
4. `datalib:0.8.0` and `datalib:0.10.0` are incompatible (different minor pre-1.0)

The initial graph built from the published graphs still contains `datalib:0.8.0` through `connector:1.1.0`.
Once `connector` is updated to the locked `1.2.0`, that node is dropped and the final graph only contains
`datalib:0.10.0`.

## Expected behavior

### All locking modes
Dependency graph should match the Dependencies.toml: connector:1.2.0 -> datalib:0.10.0.
The resolution must not report an incompatible version error for the dropped `datalib:0.8.0`.

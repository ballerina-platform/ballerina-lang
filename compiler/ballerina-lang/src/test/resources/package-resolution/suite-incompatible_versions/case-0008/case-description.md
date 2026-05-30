# Parent transitive dependency upgrade should replace child transitive deps without false conflict

1. User's package depends on `testorg/wrapper:1.0.0` which depends on `testorg/connector:1.1.0`
2. `testorg/connector:1.1.0` depends on `testorg/datalib:0.8.0`
3. Central repository has a newer `testorg/connector:1.2.0` which depends on `testorg/datalib:0.10.0`
4. `datalib:0.8.0` and `datalib:0.10.0` are incompatible (different minor pre-1.0)

## Expected behavior

### Sticky == true (HARD)
Dependency graph should keep locked versions: connector:1.1.0 -> datalib:0.8.0

### Sticky == false (SOFT)
Dependency graph should upgrade connector to 1.2.0 and use datalib:0.10.0 without conflict.
The old datalib:0.8.0 should be replaced by datalib:0.10.0 from the upgraded connector.

# Fresh project: Parent transitive dependency upgrade should replace child transitive deps without false conflict

Same scenario as case-0007 but without Dependencies.toml (fresh project).

1. User's package depends on `testorg/wrapper`
2. Central has wrapper:1.0.0 -> connector:1.1.0 -> datalib:0.8.0
3. Central also has connector:1.2.0 -> datalib:0.10.0
4. datalib:0.8.0 and datalib:0.10.0 are incompatible (different minor pre-1.0)

## Expected behavior

### HARD
Resolve latest versions without upgrades: wrapper:1.0.0 -> connector:1.1.0 -> datalib:0.8.0

### MEDIUM
Same as HARD (LOCK_MINOR prevents connector minor upgrade)

### SOFT
Upgrade connector to 1.2.0 and use datalib:0.10.0 without conflict.

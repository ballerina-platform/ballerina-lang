# Fresh project: Transitive dependency upgrade replaces incompatible pre-1.0 child dep

Same scenario as case-0011 but without Dependencies.toml (fresh project).

1. User's package depends on `testorg/pkgp`
2. Central has P:1.0.0 -> Q:1.1.0 -> R:0.1.0
3. Central also has Q:1.2.0 -> R:0.2.0

## Expected behavior

### HARD
Resolve latest: P:1.0.0 -> Q:1.1.0 -> R:0.1.0 (EXACT prevents upgrades)

### MEDIUM
Same as HARD (LOCK_MINOR prevents Q minor upgrade)

### SOFT
Upgrade Q to 1.2.0 (LOCK_MAJOR allows minor upgrade). R:0.2.0 replaces R:0.1.0.

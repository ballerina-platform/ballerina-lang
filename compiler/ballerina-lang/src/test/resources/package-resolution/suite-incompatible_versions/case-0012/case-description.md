# Transitive dependency upgrade replaces incompatible pre-1.0 child dep

1. User's package depends on `testorg/pkgp`
2. Locked: P:1.0.0 -> Q:1.1.0 -> R:0.1.0
3. Central has Q:1.2.0 -> R:0.2.0
4. R:0.1.0 and R:0.2.0 are incompatible (different minor pre-1.0)

## Expected behavior

### HARD
Keep locked versions: P:1.0.0 -> Q:1.1.0 -> R:0.1.0

### MEDIUM
LOCK_MINOR prevents Q minor upgrade. Same as HARD.

### SOFT
Upgrade Q to 1.2.0 (LOCK_MAJOR allows minor upgrade for post-1.0).
Old R:0.1.0 replaced by R:0.2.0 from upgraded Q. No conflict.

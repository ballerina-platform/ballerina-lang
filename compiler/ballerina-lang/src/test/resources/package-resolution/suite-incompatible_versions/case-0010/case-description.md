# Parent upgrade changes dependency structure with incompatible transitive dep versions

1. User's package depends on `testorg/pkgx`
2. Locked: X:1.0.0 -> Y:1.1.0 -> Z:0.2.0
3. Central has X:1.1.0 -> W:1.2.0 -> Z:0.3.0
4. Z:0.2.0 and Z:0.3.0 are incompatible (different minor pre-1.0)
5. When X is upgraded from 1.0.0 to 1.1.0, the entire dep chain changes (Y replaced by W)

## Expected behavior

### Sticky == true (HARD)
Keep locked versions: X:1.0.0 -> Y:1.1.0 -> Z:0.2.0

### Sticky == false (SOFT)
Upgrade X to 1.1.0, old chain (Y -> Z:0.2.0) replaced by new chain (W -> Z:0.3.0). No conflict.

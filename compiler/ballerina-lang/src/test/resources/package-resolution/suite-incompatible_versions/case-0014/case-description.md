# Patch upgrade changes dependency structure with incompatible transitive dep versions

1. User's package depends on `testorg/pkgh`
2. Locked: H:1.0.0 -> I:1.1.0 -> J:1.0.0 -> K:0.1.0
3. Central has I:1.1.1 -> L:1.2.0 -> K:0.2.0
4. K:0.1.0 and K:0.2.0 are incompatible (different minor pre-1.0)
5. I:1.1.1 is a patch upgrade of I:1.1.0 with a completely different dep structure

## Expected behavior

### HARD
Keep locked versions: H:1.0.0 -> I:1.1.0 -> J:1.0.0 -> K:0.1.0

### MEDIUM
LOCK_MINOR allows patch upgrade: I:1.1.0 -> I:1.1.1. Old chain (J -> K:0.1.0)
replaced by new chain (L -> K:0.2.0). No conflict.

### SOFT
Same as MEDIUM (I patch upgrade allowed by both LOCK_MINOR and LOCK_MAJOR).

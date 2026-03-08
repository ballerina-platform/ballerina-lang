# Fresh project: Patch upgrade changes dependency structure with incompatible transitive dep versions

Same scenario as case-0013 but without Dependencies.toml (fresh project).

1. User's package depends on `testorg/pkgh`
2. Central has H:1.0.0 -> I:1.1.0 -> J:1.0.0 -> K:0.1.0
3. Central has I:1.1.1 -> L:1.2.0 -> K:0.2.0

## Expected behavior

### All modes (HARD, MEDIUM, SOFT)
H:1.0.0 resolved (only version). I:1.1.0 from H's deps, then upgraded to I:1.1.1
(patch upgrade allowed by all non-HARD modes, and HARD resolves exact 1.1.0 which
then gets upgraded in completeDependencyGraph).
For HARD: I stays at 1.1.0 (EXACT), old chain preserved.
For MEDIUM/SOFT: I upgraded to 1.1.1, new chain L -> K:0.2.0.

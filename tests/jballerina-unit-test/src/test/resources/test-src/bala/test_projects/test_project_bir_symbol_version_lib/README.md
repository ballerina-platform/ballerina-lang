Test project for BIR symbol version resolution. This library (bir_symbol_version_lib) is compiled into
two bala versions (1.0.0 and 1.0.1) to verify that transitive dependency package IDs are resolved using
the currently imported symbol versions rather than the versions embedded in the BIR constant pool.
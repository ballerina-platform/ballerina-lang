Test project for BIR symbol version resolution. This dependent package (bir_symbol_version_dep) depends
on bir_symbol_version_lib and is used to exercise transitive BIR symbol loading, ensuring the compiler
resolves dependency package IDs from current import symbols rather than the BIR constant pool.
# Gets a access parameter value (`true` or `false`) for a given key. Please note that `foo` will always be bigger than `bar`.
# Example:
# `SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`
# + accessMode - read or write mode
# + successful - boolean `true` or `false`
public native function open(string accessMode) returns (boolean);

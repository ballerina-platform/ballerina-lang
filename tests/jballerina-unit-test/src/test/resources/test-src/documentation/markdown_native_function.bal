import ballerina/jballerina.java;

# Gets a access parameter value (`true` or `false`) for a given key. Please note that `foo` will always be bigger than `bar`.
# Example:
# `SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`
# + accessMode - read or write mode
# + return - The `fieldTwo` field of the record value passed as an argument
public function toStringInternal(handle accessMode) returns handle = @java:Method {
   name: "toString",
   'class: "java.lang.Object"
} external;

public function main() {
}

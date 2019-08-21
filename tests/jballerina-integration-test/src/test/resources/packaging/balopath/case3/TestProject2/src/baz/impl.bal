import ballerina/io;
import ballerinax/java;
import toml/toml4j;

public function main() {
    string tomlContent = "pet = \"cat\"";
    var tomlObj = toml4j:getToml();
    tomlObj = toml4j:read(tomlObj, java:fromString(tomlContent));
    string? petKind = java:toString(toml4j:getString(tomlObj, java:fromString("pet")));
    io:println(petKind);
}

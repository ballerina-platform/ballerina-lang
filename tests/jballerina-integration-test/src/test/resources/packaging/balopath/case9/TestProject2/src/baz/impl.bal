import ballerina/java;
import toml/toml4j;

public function main() {
    string? petKind = getPetKind();
}

public function getPetKind() returns string? {
    string tomlContent = "pet = \"cat\"";
    var tomlObj = toml4j:getToml();
    tomlObj = toml4j:read(tomlObj, java:fromString(tomlContent));
    string? petKind = java:toString(toml4j:getString(tomlObj, java:fromString("pet")));
    return petKind;
}

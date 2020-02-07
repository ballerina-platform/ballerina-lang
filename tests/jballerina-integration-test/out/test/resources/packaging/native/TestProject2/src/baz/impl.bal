import ballerina/io;
import ballerinax/java;
import bcintegrationtest/MODULE_1;

public function main() {
    string tomlContent = "pet = \"dog\"";
    var tomlObj = MODULE_1:getToml();
    tomlObj = MODULE_1:read(tomlObj, java:fromString(tomlContent));
    string? petKind = java:toString(MODULE_1:getString(tomlObj, java:fromString("pet")));
    io:println(petKind);
}

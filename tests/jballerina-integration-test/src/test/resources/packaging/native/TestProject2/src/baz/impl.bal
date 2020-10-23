import ballerina/java;
import bcintegrationtest/MODULE_1;

public function getPetKind() returns string? {
    string tomlContent = "pet = \"dog\"";
    var tomlObj = MODULE_1:getToml();
    tomlObj = MODULE_1:read(tomlObj, java:fromString(tomlContent));
    string? petKind = java:toString(MODULE_1:getString(tomlObj, java:fromString("pet")));
    return petKind;
}

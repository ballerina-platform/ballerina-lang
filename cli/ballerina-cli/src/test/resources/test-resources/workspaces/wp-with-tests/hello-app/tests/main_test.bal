import ballerina/jballerina.java;
import ballerina/test;

// Test function
@test:Config {}
function testFunction() {
    test:assertTrue(true, msg = "Failed!");
}

@test:Config {}
function testResourceFileResolvesRelativeToPackageRoot() {
    handle file = newFile(java:fromString("tests/resources/data.txt"));
    test:assertTrue(fileExists(file),
            msg = "expected 'tests/resources/data.txt' to resolve relative to the package root");
}

function newFile(handle filename) returns handle = @java:Constructor {
    'class: "java.io.File",
    paramTypes: ["java.lang.String"]
} external;

function fileExists(handle receiver) returns boolean = @java:Method {
    name: "exists",
    'class: "java/io/File",
    paramTypes: []
} external;

import ballerina/test;

# Before Suite Function

@test:Config {
}
function testFunction() {
    test:assertTrue(true, msg = "Failed!");
}

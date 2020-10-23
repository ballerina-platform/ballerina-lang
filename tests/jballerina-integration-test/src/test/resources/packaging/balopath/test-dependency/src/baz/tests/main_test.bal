import ballerina/test;

# Test function
@test:Config {
}
function testFunction() {
    test:assertTrue(true, msg = "Failed!");
}

import ballerina/test;

@test:Config {}
function testFunction() {
    test:assertTrue(true, msg = "Failed!");
}

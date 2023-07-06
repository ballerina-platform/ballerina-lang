import ballerina/test;

@test:Config {}
function testAssertTrue() {
    test:assertTrue(true, msg = "assertTrue failed");
}

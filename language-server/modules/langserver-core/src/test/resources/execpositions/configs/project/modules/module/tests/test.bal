import ballerina/test;

@test:Config {}
function testAssertFalse() {
    test:assertFalse(false, msg = "assertFalse failed");
}

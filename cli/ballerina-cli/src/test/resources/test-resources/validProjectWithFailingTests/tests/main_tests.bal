import ballerina/test;

@test:Config {}
public function testRunMain() {
    test:assertTrue(false, msg = "Failed!");
}

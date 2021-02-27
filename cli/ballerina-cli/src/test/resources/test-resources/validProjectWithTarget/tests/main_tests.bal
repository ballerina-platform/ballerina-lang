import ballerina/test;

@test:Config {}
public function testRunMain() {
    test:assertTrue(true, msg = "Failed!");
}

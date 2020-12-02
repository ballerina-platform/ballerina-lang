import ballerina/test;

@test:Config {}
public function testMain() {
    test:assertTrue(true, "Not true");
}
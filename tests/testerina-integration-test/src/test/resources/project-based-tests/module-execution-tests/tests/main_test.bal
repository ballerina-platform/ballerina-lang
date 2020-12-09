import ballerina/test;

@test:Config {}
public function testMain() {
    test:assertTrue(true, "Not true");
}

@test:Config {}
public function testMain2() {
    test:assertTrue(true, "Not true");
}

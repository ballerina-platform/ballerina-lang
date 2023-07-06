import ballerina/test;

@test:Config {}
public function testHelloFunction() {
    test:assertEquals(helloFunction(), "hello Sri Lanka from version 1.0.0");
}

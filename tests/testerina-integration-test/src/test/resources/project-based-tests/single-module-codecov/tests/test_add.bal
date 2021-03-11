import ballerina/test;

@test:Config {}
public function testAdd() {
    test:assertEquals(intAdd(5, 4), 9);
}
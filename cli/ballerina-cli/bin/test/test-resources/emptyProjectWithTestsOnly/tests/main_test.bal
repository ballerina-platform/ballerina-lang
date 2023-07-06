import ballerina/test;

@test:Config{}
public function test1() {
    test:assertEquals(1, 1);
}

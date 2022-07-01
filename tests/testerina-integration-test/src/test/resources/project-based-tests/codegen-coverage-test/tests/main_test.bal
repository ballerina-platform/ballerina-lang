import ballerina/test;

@test:Config {}
public function test1() {
    test:assertEquals(intAdd(4, 5), 9);
}

@test:Config {}
public function test2() {
    test:assertEquals(foo(2), 2);
}

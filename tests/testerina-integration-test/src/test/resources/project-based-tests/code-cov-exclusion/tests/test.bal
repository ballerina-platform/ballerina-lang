import ballerina/test;

@test:Config {}
function funcTest() {
    test:assertEquals(intMul(10, 4), 40);
}

@test:Config {}
function funcTest1() {
    test:assertEquals(40, 40);
}

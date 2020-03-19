import ballerina/test;

@test:Config {}
function testFunction2() {
    test:assertTrue(divideInt() is error);
}
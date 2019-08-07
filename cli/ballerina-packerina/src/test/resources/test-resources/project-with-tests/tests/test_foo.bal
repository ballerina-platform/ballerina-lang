import ballerina/test;

@test:Config {}
function testFoo () {
    string helloWorld = getHelloWorld();
    test:assertEquals(helloWorld, "Hello, World!");
}
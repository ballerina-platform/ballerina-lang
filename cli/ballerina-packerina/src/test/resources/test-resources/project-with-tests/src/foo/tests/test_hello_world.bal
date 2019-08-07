import ballerina/io;
import ballerina/test;

@test:Config {}
function testHelloWorlFunc() {
    string helloWorld = getHelloWorld();
    test:assertEquals(helloWorld, "Hello, World!");
}
import ballerina/test;

@test:Config {}
function testFunction() {
    string name = "John";
    string welcomeMsg = hello(name);
    test:assertEquals(welcomeMsg, "Hello, John");
}

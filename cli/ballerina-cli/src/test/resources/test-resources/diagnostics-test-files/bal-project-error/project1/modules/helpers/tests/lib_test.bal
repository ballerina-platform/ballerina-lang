import ballerina/test;

// Test function

@test:Config {}
function testFunction() {
    string name = "John";
    string welcomeMsg = hello2(name);
    test:assertEquals("Hello, John", welcomeMsg);
}

// Negative Test function

@test:Config {}
function negativeTestFunction() {
    name = "";
    string welcomeMsg = hello2(name);
    test:assertEquals("Hello, World!", welcomeMsg);
}

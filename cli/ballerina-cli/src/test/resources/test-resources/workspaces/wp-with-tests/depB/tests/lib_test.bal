import ballerina/test;

// Test function
@test:Config {}
function testFunction() {
    string name = "John";
    string welcomeMsg = hello(name);
    test:assertEquals(welcomeMsg, "Hello, John");
}

// Negative Test function

@test:Config {}
function negativeTestFunction() {
    string welcomeMsg = hello(());
    test:assertEquals(welcomeMsg, "Hello, World!");
}

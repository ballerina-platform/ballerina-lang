import ballerina/test;

# Test function
@test:Config {
}
function testFunction() {
    test:assertEquals(sayFromX(), "Hello world from module Z!" + "\nHello world from module X!");
}

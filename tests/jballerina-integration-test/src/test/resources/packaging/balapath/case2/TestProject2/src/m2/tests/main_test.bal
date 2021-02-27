import ballerina/test;

# Test function
@test:Config {
}
function testFunction() {
    test:assertEquals(print(), "bzzz" + "\nThis is org2/m1");
}

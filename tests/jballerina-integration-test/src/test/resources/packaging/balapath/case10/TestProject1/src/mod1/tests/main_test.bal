import ballerina/test;

# Test function
@test:Config {
}
function testFunction() {
    test:assertEquals(print(), "Bar");
}


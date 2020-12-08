import ballerina/test;

# Test function

@test:Config {
}
function testFunction() {
    test:assertEquals(getString1(), "Print: Hello World! from v1");
    test:assertEquals(getString2(), "Print: Hello World! from v2");
}

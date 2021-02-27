import ballerina/test;
import bcintegrationtest/MODULE_1;
import bcintegrationtest/MODULE_2;

# Test function

@test:Config {
}
function testFunction() {
    test:assertEquals(MODULE_1:print("Test me"), "Test me");
    test:assertEquals(MODULE_2:sayHello("John"), "Hello John!");
}

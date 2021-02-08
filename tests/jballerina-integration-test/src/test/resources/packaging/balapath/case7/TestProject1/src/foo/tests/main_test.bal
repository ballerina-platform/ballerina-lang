import ballerina/test;
import wso2/utils;
import ballerina/jballerina.java;

function testAcceptNothingButReturnStringTest() returns handle {
    return utils:getString();
}

# Test function
@test:Config {
}
function testFunction() {
    string result =  <string>java:toString(testAcceptNothingButReturnStringTest());
    test:assertEquals(result, "This is a test string value !!!", msg = "Interop method call failed!");
}

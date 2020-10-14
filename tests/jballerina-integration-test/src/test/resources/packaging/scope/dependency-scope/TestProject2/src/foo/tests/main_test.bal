import ballerina/test;
import ballerina/java;

public function getString() returns handle = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

# Test function

@test:Config {
}
function testFunction() {
    string result =  <string>java:toString(getString());
    test:assertEquals(result, "This is a test string value !!!", msg = "Interop method call failed!");
}

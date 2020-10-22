import ballerina/java;
import ballerina/test;

# Test function

@test:Config {
}
function testFunction() {
    string result =  <string>java:toString(getDriversAsString());
}

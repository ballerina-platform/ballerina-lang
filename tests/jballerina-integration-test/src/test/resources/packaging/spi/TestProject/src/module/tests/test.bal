import ballerina/java;
import ballerina/test;

# Test function

@test:Config {
}
function testFunction() {
    string result =  <string>java:toString(getDriversAsString());
    test:assertEquals(result, "class org.hsqldb.jdbc.JDBCDriver class org.h2.Driver ");
}

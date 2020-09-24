import ballerina/java;
import ballerina/test;

# Test function

@test:Config {
}
function testFunction() {
    string result =  <string>java:toString(getDriversAsString());
    test:assertEquals(result, "class org.h2.Driver class org.hsqldb.jdbc.JDBCDriver ");
}


import ballerina/io;
import ballerinax/java;

// Define a Ballerina function with an external function body whose implementation is provided by a Java method.
// When a vararg is used in the Ballerina function signature, corresponding values are wrapped in
// a Java array before the invocation.
public function asList(int... values) returns handle = @java:Method {
    name: "asList",
    class: "java.util.Arrays"
} external;

public function main() {
    var answer = 42;
    // If a method is non-static, the receiver instance has to be provided as the first parameter.
    var javaList = asList(1, 2, answer);
    io:println(javaList);
}





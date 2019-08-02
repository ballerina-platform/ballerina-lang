import ballerina/io;
import ballerinax/java;

public function main() {
    var answer = 42;
    // An external vararg function can be called like any other Ballerina vararg function.
    // If a method is non-static, the receiver instance has to be provided as the first parameter.
    var javaList = asList(1, 2, answer);
    io:println(javaList);
}

// Define a Ballerina function whose implementation is provided by an external Java method.
// When a vararg is used in the Ballerina function signature, corresponding values are wrapped in
// a Java array before the invocation.
public function asList(int... values) returns handle = @java:Method {
    name:"asList",
    class: "java.util.Arrays"
} external;



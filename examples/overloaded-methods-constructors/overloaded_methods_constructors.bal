import ballerina/io;
import ballerinax/java;

// This Ballerina function invokes the `java.lang.StringBuffer` constructor that takes a
// `java.lang.String` as an argument. The `java.lang.StringBuffer` class has other overloaded
// constructors that take a single parameter. Therefore you need to specify the parameter types here.
function newStringBuffer(handle str) returns handle = @java:Constructor {
    class: "java.lang.StringBuffer",
    paramTypes: ["java.lang.String"]
} external;

// The `append` method in `java.lang.StringBuffer` is overloaded many methods that take a single parameter.
// In this example, let's use two of those methods. The `appendString` function is linked with the Java `append`
// method that takes a `java.lang.String` as an argument.
function appendString(handle receiver, handle str) returns handle = @java:Method {
    name: "append",
    class: "java.lang.StringBuffer",
    paramTypes: ["java.lang.String"]
} external;

// This `appendStringBuffer` function is linked with the Java `append` method that takes a
// `java.lang.StringBuffer` as an argument.
function appendStringBuffer(handle receiver, handle strBuffer) returns handle = @java:Method {
    name: "append",
    class: "java.lang.StringBuffer",
    paramTypes: ["java.lang.StringBuffer"]
} external;

public function main() {
    // Create a new `java.lang.StringBuffer` by passing a Java string as an argument.
    var strBuffer = newStringBuffer(java:fromString("Ballerina is "));
    _ = appendString(strBuffer, java:fromString("awesome "));

    var strBufferToAppend = newStringBuffer(java:fromString("and "));
    _ = appendString(strBufferToAppend, java:fromString("fun."));

    // Append an instance of a `java.lang.StringBuffer` to another `java.lang.StringBuffer` instance.
    _ = appendStringBuffer(strBuffer, strBufferToAppend);

    string? text = java:toString(strBuffer);
    io:println(text);
}

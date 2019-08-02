import ballerina/io;
import ballerinax/java;

public function main() {
    float r = 4;
    // An external field getter can be called like any other Ballerina function.
    // If a field is non-static, the receiver instance has to be provided as the first parameter.
    float l = 2 * pi() * r;
    io:println(l);
}

// Defines a Ballerina function which will act as getter to a Java field
public function pi() returns float = @java:FieldGet {
    name:"PI",
    class:"java/lang/Math"
} external;

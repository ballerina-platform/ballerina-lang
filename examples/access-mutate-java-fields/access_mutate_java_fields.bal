import ballerina/io;
import ballerinax/java;

// Define a Ballerina function which will act as a Java field getter.
public function pi() returns float = @java:FieldGet {
    name: "PI",
    class: "java/lang/Math"
} external;

public function main() {
    float r = 4;
    // If a field is an instance field, the receiver instance has to be provided as the first parameter.
    float l = 2 * pi() * r;
    io:println(l);
}

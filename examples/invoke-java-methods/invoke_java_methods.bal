import ballerina/io;
import ballerinax/java;

// Let's create a Ballerina function to call the static method `randomUUID` in `java.util.UUID` class.
// Here the `name` field is optional if the Ballerina function name is equal to the Java counterpart.
// This method returns a handle value referring to a `java.util.UUID` instance.
function createRandomUUID() returns handle = @java:Method {
    name: "randomUUID",
    class: "java.util.UUID"
} external;

// The class `java.util.ArrayDeque` is a resizable array that allows you to add or remove an element from both sides.
// Here `newArrayDeque` function is linked with the default constructor of the `java.util.ArrayDeque` class.
function newArrayDeque() returns handle = @java:Constructor {
    class: "java.util.ArrayDeque"
} external;

// The `offer` Ballerina function is linked with the instance method `offer` in `java.util.ArrayDeque` class.
// This function inserts the element `e` at the end of the queue referred by the parameter `receiver`.
function offer(handle receiver, handle e) returns boolean = @java:Method {
    class: "java.util.ArrayDeque"
} external;

// The `poll` Ballerina function is linked with the instance method `poll` in `java.util.ArrayDeque` class.
// This function removes the head element of the queue referred by the parameter `receiver`.
function poll(handle receiver) returns handle = @java:Method {
    class: "java.util.ArrayDeque"
} external;

public function main() {
    // Create a random UUID instance by invoking the `createRandomUUID` Ballerina function.
    // This function demonstrates how you can invoke Java static function in Ballerina.
    var uuid = createRandomUUID();

    // Create a new `java.util.ArrayDeque` instance.
    var arrayDeque = newArrayDeque();

    // Ballerina strings are different from Java strings. The `fromString` function in `ballerinax/java` module converts
    // a Ballerina string value to a Java String representation. Java String is a reference type; hence,
    // this method returns a handle value referring to the created to Java string.
    _ = offer(arrayDeque, java:fromString("John"));
    _ = offer(arrayDeque, java:fromString("Jane"));
    _ = offer(arrayDeque, java:fromString("Peter"));

    var nextInLineHandle = poll(arrayDeque);
    // The `toString` function in `ballerinax/java` module creates a Ballerina string
    // representation of the Java reference value.
    string? nextInLine = java:toString(nextInLineHandle);
    io:println(nextInLine);
}

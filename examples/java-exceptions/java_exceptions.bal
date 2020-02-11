import ballerina/io;
import ballerinax/java;

// Let's create a Ballerina function to load Java classes by linking with the `forName` method of
// the `java.lang.Class`. It throws a checked exception `java.lang.ClassNotFoundException`.
// Therefore the `loadClass` ballerina function should have the `error` type as part of its return signature.
function loadClass(handle className) returns handle|error = @java:Method {
    name: "forName",
    class: "java.lang.Class"
} external;

// The class `java.util.ArrayDeque` is a resizable array that allows you to add or remove an element from both sides.
// Here `newArrayDeque` function is linked with the default constructor of the `java.util.ArrayDeque` class.
function newArrayDeque() returns handle = @java:Constructor {
    class: "java.util.ArrayDeque"
} external;

// The `offer` method in the ArrayDeque class throws a `java.lang.NullPointerException` if the element is null.
function offer(handle receiver, handle e) returns boolean = @java:Method {
    class: "java.util.ArrayDeque"
} external;

public function main() {
    // The `loadClass` function returns union of `handle` or `error`.
    var classOrError = loadClass(java:fromString("a.b.c.z.SomeClass"));
    if classOrError is error {
        io:println(classOrError);
    }

    // Here we are sending a null element to `offer` function. The `createNull` function in `ballerinax/java`
    // module creates a handle value that refers to Java null.
    var arrayDeque = newArrayDeque();
    boolean|error e = trap offer(arrayDeque, java:createNull());
    if e is error {
        io:println(e);
    }
}

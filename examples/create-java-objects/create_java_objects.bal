import ballerina/io;
import ballerina/java;

// The `@java:Constructor` annotation links the default constructor of
// the class `java.util.ArrayDeque` with this Ballerina function declaration.
function newArrayDeque() returns handle = @java:Constructor {
    class: "java.util.ArrayDeque"
} external;

// This ballerina function is linked with the `java.util.ArrayDeque` constructor that takes the initial capacity.
// There are other overloaded constructors that take a single parameter in this class.
// To link with the exact constructor, you can specify the `paramTypes` field. Refer to the `Overloaded
// Methods/Constructors` section for more details on dealing with overloaded methods and constructors.
// This function returns a `handle` value, which refers to an object of `java.util.ArrayDeque` class.
function newArrayDequeWithInitialCapacity(int numElements) returns handle = @java:Constructor {
    class: "java.util.ArrayDeque",
    paramTypes: ["int"]
} external;


public function main() {
    // Create a new `java.util.ArrayDeque` object by invoking the default constructor via `newArrayDeque` function.
    var arrayDeque = newArrayDeque();
    io:println(arrayDeque);

    // Create a new `java.util.ArrayDeque` object by invoking the constructor that takes an `int` argument
    // via `newArrayDeque` function.
    var arrayDequeWithCapacity = newArrayDequeWithInitialCapacity(10);
    io:println(arrayDequeWithCapacity);
}

import ballerina/io;
import ballerina/java;
import ballerinax/java.arrays as jarrays;

public function splitString(handle receiver, handle regex) returns handle = @java:Method {
    name: "split",
    class: "java/lang/String"
} external;

public function main() {
    // Convert Ballerina strings to Java strings before passing to Java methods.
    handle helloString = java:fromString("Hello world");
    handle regex = java:fromString(" ");

    // Invoke an external method, implemented in Java.
    handle words = splitString(helloString, regex);

    int numWords = jarrays:getLength(words);
    io:println(numWords);

    // Access an array element, pass the handle that refers to the Java array instance as the first argument.
    handle secondWord = jarrays:get(words, 1);
    io:println(secondWord);
}

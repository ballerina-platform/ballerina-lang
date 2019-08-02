import ballerina/io;
import ballerinax/java;

public function main() {
    // Converts Ballerina strings to Java strings, to be passed to Java methods
    handle helloString = java:fromString("Hello world");
    handle regex = java:fromString(" ");

    // Invokes an external method, implemented in Java
    handle words = splitString(helloString, regex);

    int numWords = java:getArrayLength(words);
    io:println(numWords);

    // Access array element, handle of the array is passed as the first argument
    handle secondWord = java:getArrayElement(words, 1);
    io:println(secondWord);
}

public function splitString(handle receiver, handle regex) returns handle = @java:Method {
    name:"split",
    class: "java/lang/String"
} external;



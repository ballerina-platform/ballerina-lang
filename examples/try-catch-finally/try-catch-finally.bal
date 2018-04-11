import ballerina/log;
import ballerina/runtime;
import ballerina/io;

function main (string[] args) {
    int result;
    //Use a try block to surround a code segment that an error may occur.
    try {
        io:println("Start dividing numbers");
        //Doing an operation that causes an error to be thrown
        result = divideNumbers(1, 0);
        //A Catch block executes, when an error is thrown from the enclosing try
        //block and the thrown error type and catch clause's error type are matched, or
        //if there is no match, then the catch is the first in the order, where thrown
        //error type and catch clause's error type are structurally equivalent.
    } catch (error err) {
        io:println("error occured: " + err.message);

    } finally {
        io:println("finally Block executed");
    }
}

function divideNumbers (int a, int b) returns int {
    return a / b;
}
import ballerina.io;
import ballerina.runtime;

function main (string[] args) {
    string[] texts = null;
    //Use a try block to surrounds a code segment that an error may occur.
    try {
        io:println("start Accessing texts");
        //Accessing a null variable 'texts' causes a NullReferenceError to be thrown.
        string value = texts[0];
        io:println(value);
        //A Catch block executes, when an error is thrown from the enclosing try
        //block and the thrown error type and catch clause's error type are matched, or
        //if there is no match, then the catch is the first in the order, where thrown
        //error type and catch clause's error type are structurally equivalent.
    } catch (error err) {
        io:println("error occured: " + err.message);
    //Catching specific error type 'NullReferenceException'.
    } catch (runtime:NullReferenceException err) {
        io:println("NullReferenceException error occurred");
    } finally {
        io:println("finally Block executed");
    }
}
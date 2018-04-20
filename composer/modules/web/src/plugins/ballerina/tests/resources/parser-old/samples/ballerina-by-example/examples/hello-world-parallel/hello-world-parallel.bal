import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"The default worker does not result in a thread of its own - it inherits a thread from the caller."}
function main (string... args) {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");

    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
    }

    worker w3 {
        system:println("Hello, World! #k");
    }
}


import ballerina/io;

// A function execution is defined using one or more workers. 
// All of the workers are automatically started when the function is invoked.
function main (string... args) {
    worker w1 {
        io:println("Hello, World! #m");
    }

    worker w2 {
        io:println("Hello, World! #n");
    }

    worker w3 {
        io:println("Hello, World! #k");
    }
}

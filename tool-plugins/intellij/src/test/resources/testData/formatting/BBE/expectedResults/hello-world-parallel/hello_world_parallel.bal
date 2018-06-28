import ballerina/io;

// Use one or more workers to define a function execution. 
// Invoke the function to start all workers automatically.
function main(string... args) {
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

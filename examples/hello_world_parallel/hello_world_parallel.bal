import ballerina/io;

// Workers don’t need to be explicitly started, they all start together when the function is invoked.
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

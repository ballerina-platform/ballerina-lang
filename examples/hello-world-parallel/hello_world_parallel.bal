import ballerina/io;

// Uses one or more workers to define a function execution. 
// Invokes the function to start all the workers.
public function main() {
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

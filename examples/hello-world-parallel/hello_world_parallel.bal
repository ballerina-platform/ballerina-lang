import ballerina/io;

// Use one or more workers to define a function execution. 
// Invoke the function to start all the workers.
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

    // Wait for all workers to finish.
    _ = wait {w1, w2, w3};
}

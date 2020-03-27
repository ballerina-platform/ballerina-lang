import ballerina/io;

// Use one or more workers to define a function execution.
// The function invocation starts all the workers.
public function main() {
    @strand {thread: "any"}
    worker w1 {
        io:println("Hello, World! #m");
    }

    @strand {thread: "any"}
    worker w2 {
        io:println("Hello, World! #n");
    }

    @strand {thread: "any"}
    worker w3 {
        io:println("Hello, World! #k");
    }
}

import ballerina/io;
import ballerina/runtime;

public function main() {
    @strand {thread: "any"}
    worker w1 {
        io:println("Hello, World! #m");
    }

    @strand {thread: "any"}
    worker w2 {
        runtime:sleep(25);
        io:println("Hello, World! #n");
    }

    @strand {thread: "any"}
    worker w3 {
        runtime:sleep(50);
        io:println("Hello, World! #k");
    }
}

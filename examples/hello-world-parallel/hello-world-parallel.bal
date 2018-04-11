import ballerina/io;

@Description {value:"Workers don’t need to be explicitly started. They start at the same time as the default worker."}
function main (string[] args) {

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

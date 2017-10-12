import ballerina.lang.system;
import ballerina.doc;

@doc:Description {value:"Workers don’t need to be explicitly started. They start at the same time as the default worker."}
function main (string[] args) {

    worker w1 {
        system:println("Hello, World! #m");
    }

    worker w2 {
        system:println("Hello, World! #n");
    }

    worker w3 {
        system:println("Hello, World! #k");
    }
}

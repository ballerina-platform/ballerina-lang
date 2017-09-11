import ballerina.lang.system;

function testReturnWorkerNegative (message msg) (int) {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");

    system:sleep(1000);
    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
    }

    worker w3 {
        system:println("Hello, World! #k");
    }

}
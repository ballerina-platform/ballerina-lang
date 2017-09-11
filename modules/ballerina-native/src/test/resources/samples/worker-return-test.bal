import ballerina.lang.system;

function testReturnDefault (message msg) (int) {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");

    system:sleep(1000);
    return 5;
    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
    }

    worker w3 {
        system:println("Hello, World! #k");
    }

}

function testReturnWorker (message msg) (int) {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");

    system:sleep(1000);
    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
        return 5;
    }

    worker w3 {
        system:println("Hello, World! #k");
    }

}

function testReturnMultipleWorkers (message msg) (int) {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");
    return 5;
    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
        return 6;
    }

    worker w3 {
        system:println("Hello, World! #k");
        return 7;
    }

}

function testReturnVoidMultipleWorkers (message msg)  {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");
    return;
    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
        return;
    }

    worker w3 {
        system:println("Hello, World! #k");
        return;
    }

}

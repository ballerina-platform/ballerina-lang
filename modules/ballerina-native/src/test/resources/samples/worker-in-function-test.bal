import ballerina.lang.system;
import ballerina.lang.messages;

function testSimpleWorker(message msg)(message) {
    message q;
    q = testSimpleWorkerVM(msg);
    return q;
}

function testParallelWorkers (message msg) (message) {
    //This statement will be executed by the default worker.
    system:println("Hello, World! #m");

    message m = {};
    return m;
    //Workers don’t need to be explicitly started. They start at the same time as the default worker.
    worker w2 {
        system:println("Hello, World! #n");
    }

    worker w3 {
        system:println("Hello, World! #k");
    }

}

function testSimpleWorkerVM(message msg)(message) {
    message result;
    //message msg = {};
    int x = 100;
    float y;
    map p = { "a" : 1, "b" : 2};
    p, msg, x -> sampleWorker;
    system:println("Worker calling function test started");
    y, result <- sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    int aVal;
    aVal, _ = (int)p["a"];
    system:println("Value received from worker is " + aVal);
    return result;

    worker sampleWorker {
    message result;
    message m;
    int a;
    map q;
    float b = 12.34;
    q, m, a <- default;
    q["a"] = 12;
    int intVal;
    intVal, _ = (int)q["a"];
    system:println("passed in value is " + intVal);
    json j;
    j = {"name":"chanaka"};
    messages:setJsonPayload(m, j);
    b, m -> default;
}

}

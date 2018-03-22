import ballerina/lang.system;
import ballerina/lang.messages;

function testSimpleWorker(message msg)(message) {
    message q;
    q = testSimpleWorkerVM(msg);
    return q;
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

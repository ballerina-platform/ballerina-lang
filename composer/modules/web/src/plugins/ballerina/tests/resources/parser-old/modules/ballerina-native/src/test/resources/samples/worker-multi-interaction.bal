import ballerina/lang.system;


function testWorkerInVM()(boolean c) {
    boolean q;
    q = testWorkerScenario1();
    return q;
}
function testWorkerScenario1()(boolean c) {
    system:println("Hello, World from default worker");
    true -> w3;
    true -> w2;
    //boolean b;
    c <- w2;
    c <- w3;
    return c;
    worker w2 {
        boolean b;
        b <- default;
        system:println("Hello, World from worker w2");
        true -> default;
        return;
    }
    worker w3 {
        boolean b;
        b <- default;
        system:println("Hello, World! from worker w3");
        true -> default;
        return;
    }
}



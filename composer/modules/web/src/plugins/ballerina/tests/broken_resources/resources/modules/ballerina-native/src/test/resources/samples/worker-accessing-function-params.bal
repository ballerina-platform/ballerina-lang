import ballerina/lang.system;

function testFunctionArgumentAccessFromWorker(int x)(int q) {
    q = testFunctionArgumentAccessFromWorkerVM(x);
    return q;
}

function testFunctionArgumentAccessFromWorkerVM(int x)(int q) {
    system:println("Argument in default worker " + x);
    //int y;
    map nn;
    nn, q <- sampleWorker2;
    system:println("Argument received from sampleWorker2 " + q);
    system:println(nn["name"]);
    return q;

    worker sampleWorker2 {
    system:println("Argument in sampleWorker2 " + x);
    int p = 1000 + x;
    x = 3333;
    map mm = {"name":"chanaka", "age":"32"};
    mm, p -> default;
    }
}





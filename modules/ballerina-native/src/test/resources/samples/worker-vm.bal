import ballerina.lang.system;

function testVMWorker(int x) {
    system:println("Argument in default worker " + x);
    int y = 100;
    //return;
    worker W1{
    system:println("Argument in W1 " + x);
    }

    worker W2{
    system:println("Argument in W2 " + x);
    }
}
import ballerina/io;

xmlns "http://ballerina.com/aa" as ns0;

# Prints `Hello World`.
function testPackageFunction(string param1) {
    int localVar1 = 12;
    string localVar2 = "Hello"; 
    boolean localVar3 = false;
    float localVar5 = 12.0;
    int[] localVar4;
    io:println("Hello World!");
}

function testFunctionWithWorker() {
    worker wr1 {
        int workerLocal1 = 12;
    }
    
    worker wr2 {
        int workerLocal2 = 12;
    }
}

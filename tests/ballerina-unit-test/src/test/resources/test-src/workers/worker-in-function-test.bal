import ballerina/io;
import ballerina/runtime;

function testSimpleWorker(string msg) returns string {
    return testSimpleWorkerVM(msg);
}

type TStruct record {
    string msg;
};

function testSimpleWorkerVM(string msg) returns string {
    worker default {
        "a" -> sampleWorker;
        string result = "";
        result <- sampleWorker;
        return result;
    }

    worker sampleWorker {
        string m = "";
        m <- default;
        msg -> default;
    }

}

function testMultipleReturnsVM() returns int {
    worker w1 {
        return 1;
    }

    worker w2 {
        return 2;
    }
}

function testMultipleNilReturnsVM() {
    test();
    io:println("Done");
}

function test() {
    worker w1 {
        return;
    }

    worker w2 {
        return;
    }
}


function testSimpleWorker(string msg) returns string {
    return testSimpleWorkerVM(msg);
}

type TStruct record {
    string msg;
};

function testSimpleWorkerVM(string msg) returns string {
    @strand{thread:"any"}
    worker first returns string {
        "a" -> sampleWorker;
        string result = "";
        result = <- sampleWorker;
        return result;
    }

    @strand{thread:"any"}
    worker sampleWorker {
        string m = "";
        m = <- first;
        msg -> first;
    }

    return wait first;
}


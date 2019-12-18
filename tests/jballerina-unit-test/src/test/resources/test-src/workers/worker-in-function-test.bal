
function testSimpleWorker(string msg) returns string {
    return testSimpleWorkerVM(msg);
}

type TStruct record {
    string msg;
};

function testSimpleWorkerVM(string msg) returns string {
    @concurrent{}
    worker first returns string {
        "a" -> sampleWorker;
        string result = "";
        result = <- sampleWorker;
        return result;
    }

    @concurrent{}
    worker sampleWorker {
        string m = "";
        m = <- first;
        msg -> first;
    }

    return wait first;
}


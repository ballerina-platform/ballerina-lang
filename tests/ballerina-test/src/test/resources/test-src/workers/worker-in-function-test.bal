function testSimpleWorker (string msg) returns string {
    return testSimpleWorkerVM(msg);
}

type TStruct {
    string msg;
};

function testSimpleWorkerVM (string msg) returns string {
    worker default {
        "a" -> sampleWorker;
        string result;
        result <- sampleWorker;
        return result;
    }

    worker sampleWorker {
        string m;
        m <- default;
        msg -> default;
    }

}

function testSimpleWorker (string msg) (string) {
    return testSimpleWorkerVM(msg);
}

struct TStruct {
    string msg;
}

function testSimpleWorkerVM (string msg) (string ) {
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

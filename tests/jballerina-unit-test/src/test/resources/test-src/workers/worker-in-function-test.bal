
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

type Result record {
    string a;
    string b;
};

function fetch(string path) returns string {
    return path;
}

function multiFetch(string pathA, string pathB) returns Result|error {
    worker WA returns string {
        return fetch(pathA);
    }

    worker WB returns string {
            return fetch("pathB");
    }

    return wait {a: WA, b:WB};
}

public function testReturnWaitForAll() returns string|error {
    Result res = check multiFetch("pathA", "pathB");
    return res.a;
}

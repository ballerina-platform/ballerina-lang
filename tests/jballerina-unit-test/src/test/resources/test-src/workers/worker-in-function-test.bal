
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

type ResultWithError record {
    string|error a;
    string|error b;
};

function fetch(string path) returns string {
    return path;
}

function fetchWithError(string path) returns string|error {
    return path;
}

function multiFetch1(string pathA, string pathB) returns Result|error {
    worker WA returns string {
        return fetch(pathA);
    }

    worker WB returns string {
            return fetch("pathB");
    }

    return wait {a: WA, b:WB};
}

public function testReturnWaitForAll1() returns string|error {
    Result res = check multiFetch1("pathA", "pathB");
    return res.a;
}

function multiFetch2(string pathA, string pathB) returns ResultWithError|error {
    worker WA returns string|error {
        return fetchWithError(pathA);
    }

    worker WB returns string|error {
            return fetchWithError("pathB");
    }

    return wait {a: WA, b:WB};
}

public function testReturnWaitForAll2() returns string|error {
    ResultWithError res = check multiFetch1("pathA", "pathB");
    return res.a;
}

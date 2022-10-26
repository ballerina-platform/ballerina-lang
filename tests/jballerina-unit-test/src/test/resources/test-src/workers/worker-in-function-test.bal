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

public function testReturnWaitForAll1() {
    Result|error res = multiFetch1("pathA", "pathB");
    if res is Result {
        assertEquality(res.a, "pathA");
        assertEquality(res.b, "pathB");
    }
    else {
        assertEquality(res, {a: "pathA", b: "pathB"});
    }
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

public function testReturnWaitForAll2() {
    ResultWithError|error res = multiFetch1("pathA", "pathB");
    if res is ResultWithError {
        assertEquality(res.a, "pathA");
        assertEquality(res.b, "pathB");
    }
    else {
        assertEquality(res, {a: "pathA", b: "pathB"});
    }
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

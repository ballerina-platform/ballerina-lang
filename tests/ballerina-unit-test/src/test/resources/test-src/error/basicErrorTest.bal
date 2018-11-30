function errorConstructReasonTest() returns (error, error, error, string, any, string) {
    error er1 = error("error1");

    string s = "error2";
    error er2 = error(s);

    map<string> m = { k1: "error3" };
    error er3 = error(m.k1);

    return (er1, er2, er3, er1.reason(), er2.reason(), er3.reason());
}

function errorConstructDetailTest() returns (error, error, error, any, any, any) {
    error er1 = error("error1", { message: "msg1" });

    string s = "error2";
    map<any> m2 = { message: "msg2" };
    error er2 = error(s, m2);

    map<string> reason = { k1: "error3" };
    map<string> details = { message: "msg3" };
    error er3 = error(reason.k1, details);

    return (er1, er2, er3, er1.detail(), er2.detail(), er3.detail());
}

function errorPanicTest(int i) returns string {
    string val = errorPanicCallee(i);
    return val;
}

function errorPanicCallee(int i) returns string {
    if (i > 10) {
        error err = error("largeNumber", { message: "large number" });
        panic err;
    }
    return "done";
}

function errorTrapTest(int i) returns string|error {
    string|error val = trap errorPanicCallee(i);
    return val;
}

type TrxError error<string, TrxErrorData>;

type TrxErrorData record {
    string message = "";
    error? cause = ();
    string data = "";
    !...
};

public function testCustomErrorDetails() returns error {
    TrxError err = error("trxErr", { data: "test" });
    return err;
}

public function testCustomErrorDetails2() returns string {
    TrxError err = error("trxErr", { data: "test" });
    TrxErrorData errorData = err.detail();
    return errorData.data;
}

public function testErrorWithErrorConstructor() returns string {
    error<string, TrxErrorData> err = error("trxErr", { data: "test" });
    TrxErrorData errorData = err.detail();
    return errorData.data;
}

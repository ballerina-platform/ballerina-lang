import projectls.lsmod1;

type ErrorData record {
    string errorCode;
};

type ApplicationError error<lsmod1:Mod1Rec>;

type MyError error;

public function checkError() returns ApplicationError? {
    return error ApplicationError("Custom error", key = "123");
}

public function f1() returns MyError? {
    return error MyError("message");
}

public function f2() returns error? {
    return error lsmod1:Mod1Error("message");
}

function test1() returns error? {
    error er = error error:Retriable("");
}

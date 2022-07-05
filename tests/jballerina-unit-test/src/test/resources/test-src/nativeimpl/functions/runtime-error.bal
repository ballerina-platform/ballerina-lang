import ballerina/lang.runtime;

function testGetCallStack () returns (runtime:StackFrame[]) {
    return level1Function();
}

function level1Function () returns (runtime:StackFrame[]) {
    return level2Function();
}

function level2Function () returns (runtime:StackFrame[]) {
    return runtime:getStackTrace();
}

function testErrorCallStack() returns error:StackFrame[]? {
    var e = trap level1Error(-10);
    if (e is error) {
        return e.stackTrace();
    } else {
        return ();
    }
}

function level1Error (int value) returns (int) {
    return level2Error(value);
}

function level2Error (int value) returns (int) {
    if (value < 0) {
        error e = error("less than zero");
        panic e;
    }
    return value;
}

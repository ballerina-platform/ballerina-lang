import ballerina/runtime;

function testGetCallStack () returns (runtime:CallStackElement[]) {
    return level1Function();
}

function level1Function () returns (runtime:CallStackElement[]) {
    return level2Function();
}

function level2Function () returns (runtime:CallStackElement[]) {
    return runtime:getCallStack();
}

function testErrorStackFrame () returns (runtime:CallStackElement[]|()) {
    var e = trap level1Error(-10);
    if (e is error) {
        return runtime:getErrorCallStackFrame(e);
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

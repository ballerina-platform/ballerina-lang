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

function testErrorStackFrame () returns (runtime:CallStackElement|()) {
    try {
        int i = level1Error(-10);
    } catch (error e) {
        return runtime:getErrorCallStackFrame(e);
    }
    return ();
}

function level1Error (int value) returns (int) {
    return level2Error(value);
}

function level2Error (int value) returns (int) {
    if (value < 0) {
        error e = {message:"less than zero"};
        throw e;
    }
    return value;
}

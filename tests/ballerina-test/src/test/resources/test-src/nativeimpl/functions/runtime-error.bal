import ballerina.runtime;

function testGetCallStack () returns (runtime:CallStackElement[] trace) {
    return level1Function();
}

function level1Function () returns (runtime:CallStackElement[] trace) {
    return level2Function();
}

function level2Function () returns (runtime:CallStackElement[] trace) {
    return runtime:getCallStack();
}

function testErrorStackFrame () returns (runtime:CallStackElement trace) {
    try {
        int i = level1Error(-10);
    } catch (error e) {
        return runtime:getErrorCallStackFrame(e);
    }
    return null;
}

function level1Error (int value) returns (int outValue) {
    outValue = level2Error(value);
    return;
}

function level2Error (int value) returns (int outValue) {
    if (value < 0) {
        error e = {message:"less than zero"};
        throw e;
    }
    outValue = value;
    return;
}

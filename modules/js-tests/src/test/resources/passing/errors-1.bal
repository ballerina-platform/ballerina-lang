package ballerina.builtin;

public struct NullReferenceException {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

public struct IllegalStateException {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

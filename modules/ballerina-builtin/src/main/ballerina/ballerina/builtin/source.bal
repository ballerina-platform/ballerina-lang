package ballerina.builtin;

public struct NullReferenceException {
    string msg;
    error cause;
    stackFrame[] stackTrace;
}

public struct IllegalStateException {
    string msg;
    error cause;
    stackFrame[] stackTrace;
}

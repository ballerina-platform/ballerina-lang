package ballerina.builtin;

public struct error {
    string msg;
    error cause;
    stackFrame[] stackTrace;
}

public struct stackFrame {
    string caller;
    string packageName;
    string fileName;
    int lineNumber;
}


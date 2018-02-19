package ballerina.runtime;

public struct NullReferenceException {
    string message;
    error cause;
}

public struct IllegalStateException {
    string message;
    error cause;
}

public struct CallStackElement {
    string workerName;
    string packageName;
    string fileName;
    int lineNumber;
}

public native function getCallStack ()(CallStackElement[]);

public native function getErrorCallStack (error e)(CallStackElement[]);
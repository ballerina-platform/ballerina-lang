
public struct error {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
}

public struct StackFrame {
    string caller;
    string packageName;
    string fileName;
    int lineNumber;
}

public struct TypeCastError {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
    string sourceType;
    string targetType;
}

public struct TypeConversionError {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
    string sourceType;
    string targetType;
}


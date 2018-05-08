
public struct error {
    string msg;
    error? cause;
    stackFrame[] stackTrace;
}

public struct stackFrame {
    string caller;
    string packageName;
    string fileName;
    int lineNumber;
}

public struct TypeCastError {
    string msg;
    error? cause;
    stackFrame[] stackTrace;
    string sourceType;
    string targetType;
}

public struct TypeConversionError {
    string msg;
    error? cause;
    stackFrame[] stackTrace;
    string sourceType;
    string targetType;
}

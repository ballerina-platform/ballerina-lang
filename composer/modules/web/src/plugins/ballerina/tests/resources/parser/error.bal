
struct Error {
    string msg;
    Error cause;
}

struct StackTrace {
    StackTraceItem[] items;
}

struct StackTraceItem {
    string caller;
    string packageName;
    string fileName;
    int lineNumber;
}

struct CastError {
    string msg;
    Error cause;
    string sourceType;
    string targetType;
}

struct TypeCastError {
    string msg;
    Error cause;
    string sourceType;
    string targetType;
}

struct TypeConversionError {
    string msg;
    Error cause;
    string sourceType;
    string targetType;
}

struct NullReferenceError {
    string msg;
    Error cause;
}
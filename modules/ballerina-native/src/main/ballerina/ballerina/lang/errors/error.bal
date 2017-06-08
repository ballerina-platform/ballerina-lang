package ballerina.lang.errors;

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

native function getStackTrace(Error err)(StackTrace);

function toString(StackTraceItem item)(string){
    return item.packageName + ":" + item.caller + "(" + item.fileName + ":" + item.lineNumber + ")";
}

struct CastError {
    string msg;
    Error cause;
    string sourceType;
    string targetType;
}
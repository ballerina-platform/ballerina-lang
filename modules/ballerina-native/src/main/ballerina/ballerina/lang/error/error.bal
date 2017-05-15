package ballerina.lang.error;

struct error {
    string msg;
    error cause;
}

struct stackTrace {
    stackTraceItem[] trace;
}

struct stackTraceItem {
    string caller;
    string packageName;
    string fileName;
    int lineNumber;
}

native function getStackTrace(error err)(stackTrace);

function toString(stackTraceItem item)(string){
    // native function printStackTrace(error err);
    return item.packageName + ":" + item.caller + "(" + item.fileName + ":" + item.lineNumber + ")";
}

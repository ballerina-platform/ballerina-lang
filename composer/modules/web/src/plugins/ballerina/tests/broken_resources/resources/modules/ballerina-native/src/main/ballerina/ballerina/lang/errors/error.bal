
import ballerina/doc;

@doc:Description {value:"Ballerina Error struct represents an Error in a Ballerina program. Error struct is the root struct for all errors in Ballerina Language and any other error should structurally equivalent to this. Only Error struct or equivalent can be thrown using throw statement or caught using catch clause in Ballerina."}
@doc:Field {value:"msg: An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this Error to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this error originated from itself."}
struct Error {
    string msg;
    Error cause;
}

@doc:Description {value:"Represents the invocation stack of a Ballerina program"}
@doc:Field {value:"items: An array of StackTraceItem ordered by oldest to latest invocation."}
struct StackTrace {
    StackTraceItem[] items;
}

@doc:Description {value:"An element in a stack trace which represents the invocation information about a function/action/resource."}
@doc:Field {value:"caller: Caller's name of the current function/action/resource"}
@doc:Field {value:"packageName: Package name of the current function/action/resource"}
@doc:Field {value:"fileName: File name of the current function/action/resource"}
@doc:Field {value:"lineNumber: Last executed line number of the current function/action/resource"}
struct StackTraceItem {
    string caller;
    string packageName;
    string fileName;
    int lineNumber;
}

@doc:Description {value:"Provide access to StackTrace of an Error. StackTrace available only when an error is thrown. Otherwise, returns a null reference."}
@doc:Param {value:"err : The Error struct"}
@doc:Return {value:"ballerina.lang.errors:StackTrace: StackTrace struct containing StackTrace of the error."}
native function getStackTrace(Error err)(StackTrace);

@doc:Description {value:"Converts StackTraceItem details into a single string."}
@doc:Param {value:"err : The StackTraceItem struct"}
@doc:Return {value:"string: StackTraceItem details as a string."}
function toString(StackTraceItem item)(string){
    string packageName = "";
    if (item.packageName == ".") {
        packageName = item.packageName + ":";
    }
    return packageName + item.caller + "(" + item.fileName + ":" + item.lineNumber + ")";
}

@doc:Description {value:"Represents an error occurred during a TypeCasting."}
@doc:Field {value:"msg:  An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this TypeCastingError to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this TypeCastingError originated from itself."}
@doc:Field {value:"sourceType: Source type"}
@doc:Field {value:"targetType: Target type"}
struct TypeCastError {
    string msg;
    Error cause;
    string sourceType;
    string targetType;
}

@doc:Description {value:"Represents an error occurred during a TypeConversion."}
@doc:Field {value:"msg:  An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this TypeConversionError to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this TypeConversionError originated from itself."}
@doc:Field {value:"sourceType: Source type"}
@doc:Field {value:"targetType: Target type"}
struct TypeConversionError {
    string msg;
    Error cause;
    string sourceType;
    string targetType;
}

@doc:Description {value:"Represents an error occurred when accessing filed of a referance value which is null"}
@doc:Field {value:"msg:  An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this NullReferenceError to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this NullReferenceError originated from itself."}
struct NullReferenceError {
    string msg;
    Error cause;
}
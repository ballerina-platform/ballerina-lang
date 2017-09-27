package ballerina.lang.errors;

import ballerina.doc;

@doc:Description {value:"Represents an error occurred during a TypeCasting."}
@doc:Field {value:"msg:  An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this TypeCastingError to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this TypeCastingError originated from itself."}
@doc:Field {value:"sourceType: Source type"}
@doc:Field {value:"targetType: Target type"}
public struct TypeCastError {
    string msg;
    error cause;
    stackFrame[] stackTrace;
    string sourceType;
    string targetType;
}

@doc:Description {value:"Represents an error occurred during a TypeConversion."}
@doc:Field {value:"msg:  An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this TypeConversionError to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this TypeConversionError originated from itself."}
@doc:Field {value:"sourceType: Source type"}
@doc:Field {value:"targetType: Target type"}
public struct TypeConversionError {
    string msg;
    error cause;
    stackFrame[] stackTrace;
    string sourceType;
    string targetType;
}

@doc:Description {value:"Represents an error occurred when accessing filed of a referance value which is null"}
@doc:Field {value:"msg:  An error message explaining about the error."}
@doc:Field {value:"cause: The error that caused this NullReferenceError to get thrown. the null reference is permitted. if the value is null, either cause is unknown or this NullReferenceError originated from itself."}
public struct NullReferenceError {
    string msg;
    error cause;
    stackFrame[] stackTrace;
}
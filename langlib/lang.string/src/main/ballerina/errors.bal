
import ballerina/lang.'runtime as runtime_error;

// IndexOutOfRangeError
public type StringIndexOutOfRange distinct runtime_error:IndexOutOfRange;

// StringOperationError
public type InvalidSubstringRange distinct runtime_error:StringOperationError;
public type NotEnoughFormatArgumentsError distinct runtime_error:StringOperationError;
public type InvalidFormatSpecifierError distinct runtime_error:StringOperationError;
public type IllegalFormatConversionError distinct runtime_error:StringOperationError;
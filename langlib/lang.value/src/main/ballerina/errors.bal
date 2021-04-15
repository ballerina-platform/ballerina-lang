import ballerina/lang.'runtime as runtime_error;

// ConversionError
public type CannotConvertNil distinct runtime_error:ConversionError;

// MergeJsonError
public type MergeJsonError distinct runtime_error:JSONOperationError;

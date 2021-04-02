import ballerina/lang.'runtime as runtime_error;

// ConversionError
public type CyclicValueReference distinct runtime_error:ConversionError;
public type IncompatibleConvertOperation distinct runtime_error:ConversionError;
public type CannotConvertNil distinct runtime_error:ConversionError;

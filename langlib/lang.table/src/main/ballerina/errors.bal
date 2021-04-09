import ballerina/lang.'runtime as runtime_error;

// KeyNotFoundError
public type TableKeyNotFound distinct runtime_error:KeyNotFound;

// InherentTypeViolation
public type InherentTableTypeViolation distinct runtime_error:InherentTypeViolation;

// KeyConstraintViolationError
public type TableKeyConstraintViolation distinct runtime_error:KeyConstraintViolation;

// ConversionError
public type CyclicValueReference distinct runtime_error:ConversionError;
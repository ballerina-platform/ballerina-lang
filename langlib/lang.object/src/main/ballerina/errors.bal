import ballerina/lang.'runtime as runtime_error;

// InvalidUpdate
public type InvalidFinalFieldUpdate distinct runtime_error:InvalidUpdate;
public type InvalidReadonlyValueUpdate distinct runtime_error:InvalidUpdate;

// InherentTypeViolation
public type InvalidObjectFieldValue distinct runtime_error:InherentTypeViolation;

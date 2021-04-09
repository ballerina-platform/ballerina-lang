
import ballerina/lang.'runtime as runtime_error;

// KeyNotFound
public type MapKeyNotFound distinct runtime_error:KeyNotFound;
public type InvalidRecordFieldAccess distinct runtime_error:KeyNotFound;

// InherentTypeViolation
public type InvalidMapInsertion distinct runtime_error:InherentTypeViolation;
public type InvalidRecordFieldAddition distinct runtime_error:InherentTypeViolation;
public type JsonSetError distinct runtime_error:InherentTypeViolation;
public type JsonGetError distinct runtime_error:InherentTypeViolation;

// InvalidUpdate
public type InvalidReadonlyFieldUpdate distinct runtime_error:InvalidUpdate;


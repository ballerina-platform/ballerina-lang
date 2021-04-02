import ballerina/lang.'runtime as runtime_error;

// IndexOutOfRangeError
public type IndexNumberTooLarge distinct runtime_error:IndexOutOfRange;
public type ArrayIndexOutOfRange distinct runtime_error:IndexOutOfRange;
public type TupleIndexOutOfRange distinct runtime_error:IndexOutOfRange;

// IllegalListInsertionError
public type IllegalArrayInsertion distinct runtime_error:IllegalListInsertion;
public type IllegalTupleInsertion distinct runtime_error:IllegalListInsertion;

// InherentTypeViolation
public type IllegalArraySize distinct runtime_error:InherentTypeViolation;
public type IllegalTupleSize distinct runtime_error:InherentTypeViolation;
public type IllegalTupleWithRestTypeSize distinct runtime_error:InherentTypeViolation;
import ballerina/jballerina.java;

public type Employee record {
    string name = "";
};

public function acceptRecordAndRecordReturnWhichThrowsCheckedException(Employee e, string newVal) returns Employee = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

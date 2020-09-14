import ballerina/java;

public function acceptIntUnionReturnWhichThrowsCheckedException(int s) returns int|string|float|boolean = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

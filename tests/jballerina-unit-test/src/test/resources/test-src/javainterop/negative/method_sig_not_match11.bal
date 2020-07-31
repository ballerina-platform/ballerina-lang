import ballerina/java;

function returnStringForBUnionFromJava() returns int|float|string = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


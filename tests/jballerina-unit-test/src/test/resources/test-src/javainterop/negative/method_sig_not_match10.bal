import ballerina/java;

function decimalParamAndReturnAsObject(decimal a1) returns decimal = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


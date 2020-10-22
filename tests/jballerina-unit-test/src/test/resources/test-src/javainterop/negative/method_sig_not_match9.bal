import ballerina/java;

function decimalParamAsObjectAndReturn(decimal a1) returns decimal = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


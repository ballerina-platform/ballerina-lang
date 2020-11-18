import ballerina/java;

function getIntFromJsonInt(int|string x) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


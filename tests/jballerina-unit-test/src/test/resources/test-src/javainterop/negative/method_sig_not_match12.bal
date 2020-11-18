import ballerina/java;

function getIntFromJsonInt(json x) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


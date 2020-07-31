import ballerina/java;

public function getArrayValueFromMapWhichThrowsCheckedException(string key, map<int> mapValue) returns int[] = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

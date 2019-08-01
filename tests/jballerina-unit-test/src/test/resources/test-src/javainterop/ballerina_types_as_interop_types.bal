import ballerina/java;

public function interopWithArrayAndMap() returns int[] {
    map<int> mapVal = { "keyVal":8};
    return getArrayValueFromMap("keyVal", mapVal);
}

public function getArrayValueFromMap(string key, map<int> mapValue) returns int[] = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;



import ballerina/jballerina.java;

public function interopFieldGetError() {
    boolean ans = isAvailable(2);
}

function isAvailable(int arg) returns boolean = @java:FieldGet {
    name:"isAvailable",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

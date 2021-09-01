import ballerina/jballerina.java;

public function interopFieldGetError() {
    boolean ans = isEmpty(2);
}

function isEmpty(int arg) returns boolean = @java:FieldGet {
    name:"isEmpty",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

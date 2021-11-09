import ballerina/jballerina.java;

public function interopFieldGetError() {
    boolean ans = isEmpty();
}

function isEmpty() returns boolean = @java:FieldGet {
    name:"isEmpty",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

import ballerina/jballerina.java;

public function interopWithFieldSetError() {
    isEmpty(true);
}

function isEmpty(boolean arg) = @java:FieldSet {
    name:"isEmpty",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

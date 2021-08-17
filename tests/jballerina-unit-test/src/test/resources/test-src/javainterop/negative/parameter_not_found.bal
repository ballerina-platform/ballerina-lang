import ballerina/jballerina.java;

public function interopFieldSetNoParameterError() {
    handle JavaFieldAccessMutateObj = newJavaFieldAccessMutate();
    isEmpty(JavaFieldAccessMutateObj);
}

function newJavaFieldAccessMutate () returns handle = @java:Constructor {
    'class: "org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

function isEmpty(handle receiver) = @java:FieldSet {
    name:"isEmpty",
    'class:"org/ballerinalang/nativeimpl/jvm/tests/JavaFieldAccessMutate"
} external;

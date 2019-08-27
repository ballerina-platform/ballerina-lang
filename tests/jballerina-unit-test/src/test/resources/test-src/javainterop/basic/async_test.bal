import ballerinax/java;

function asyncTest() returns int {
    return countSlowly();
}

// Interop functions
public function countSlowly() returns int = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/AsyncInterop"
} external;

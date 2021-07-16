import ballerina/jballerina.java;
import ballerina/lang.runtime;

function asyncTest() returns int {
    return countSlowly();
}

// Interop functions
public function countSlowly() returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/AsyncInterop"
} external;

public function main() {
    completeFutureMoreThanOnce();
    runtime:sleep(1);
}
public function completeFutureMoreThanOnce() = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/AsyncInterop"
} external;

import ballerina/jballerina.java;


function returnValueTest() returns int {
    IntFunction w = new;
    return accumulate(w, 3, 5); // 3*3 + 4*4 + 5*5
}

public class IntFunction {

    public function invoke(int i) returns int {
        return i * i;
    }
}

// Interop functions
public function accumulate(IntFunction i, int 'from, int 'to) returns int = @java:Method {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/Accumulator"
} external;

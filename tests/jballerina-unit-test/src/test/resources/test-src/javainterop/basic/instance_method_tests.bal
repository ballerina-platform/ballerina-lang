import ballerinax/java;

function testAcceptNothingAndReturnNothing(handle receiver) {
    increaseCounterByOne(receiver);
}

function testAcceptNothingAndReturnVoidThrows(handle receiver) returns error? {
    return testThrowsWithVoid(receiver);
}

function testAcceptNothingAndReturnVoidThrowsReturn(handle receiver) returns error? {
    return testThrowsWithVoidReturn(receiver);
}

function testHandleOrErrorReturn(handle receiver) returns handle|error {
    return handleOrErrorReturn(receiver);
}

function testHandleOrErrorReturnThrows(handle receiver) returns handle|error {
    return handleOrErrorReturnThrows(receiver);
}

function testInteropFunctionWithDifferentName(handle receiver) {
    interopFunctionWithDifferentName(receiver);
}

function testAcceptNothingButReturnSomething(handle receiver) returns handle {
    return getCounter(receiver);
}

function testAcceptSomethingButReturnNothing(handle receiver, handle h) {
    setCounterValue(receiver, h);
}

function testAcceptSomethingAndReturnSomething(handle receiver, handle h) returns handle {
    return setAndGetCounterValue(receiver, h);
}

function testAcceptTwoParamsAndReturnSomething(handle receiver, handle h1, handle h2) returns handle {
    return setTwiceAndGetCounterValue(receiver, h1, h2);
}



// Interop functions

public function increaseCounterByOne(handle receiver) = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function interopFunctionWithDifferentName(handle receiver) = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods",
    name:"increaseCounterByOne"
} external;

public function getCounter(handle receiver) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setCounterValue(handle receiver, handle h) = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValue(handle receiver, handle h) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setTwiceAndGetCounterValue(handle receiver, handle h1, handle h2) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValueWhichThrowsCheckedException(handle receiver, handle h) returns handle | error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setAndGetCounterValueWhichThrowsUncheckedException(handle receiver, handle h) returns handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setGetCounterValueWhichThrowsCheckedException(handle receiver, float f) returns int | error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function setGetCounterValueWhichThrowsUncheckedException(handle receiver, float f) returns int = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function testThrowsWithVoid(handle receiver) returns error? = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function testThrowsWithVoidReturn(handle receiver) returns error? = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorReturn(handle receiver) returns handle|error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorReturnThrows(handle receiver) returns handle|error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;


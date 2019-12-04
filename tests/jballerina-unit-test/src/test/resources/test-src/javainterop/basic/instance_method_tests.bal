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

function testHandleOrErrorWithObjectReturn(handle receiver) returns error|handle {
     return handleOrErrorWithObjectReturn(receiver);
}

function testPrimitiveOrErrorReturn(handle receiver) returns error|float {
     return primitiveOrErrorReturn(receiver);
}

function testPrimitiveOrErrorReturnThrows(handle receiver) returns error|float {
     return primitiveOrErrorReturnThrows(receiver);
}

function testUnionWithErrorReturnByte(handle receiver) returns error|int|boolean|byte|handle {
     return unionWithErrorReturnByte(receiver);
}

function testUnionWithErrorReturnThrows(handle receiver) returns error|int|boolean|byte|handle {
     return unionWithErrorReturnThrows(receiver);
}

function testUnionWithErrorReturnHandle(handle receiver) returns error|int|boolean|byte|handle {
     return unionWithErrorReturnHandle(receiver);
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

public function handleOrErrorWithObjectReturn(handle receiver) returns handle|error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function handleOrErrorWithObjectReturnThrows(handle receiver) returns handle|error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function primitiveOrErrorReturn(handle receiver) returns float|error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function primitiveOrErrorReturnThrows(handle receiver) returns float|error = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnByte(handle receiver) returns error|int|boolean|byte|handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnThrows(handle receiver) returns error|int|boolean|byte|handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function unionWithErrorReturnHandle(handle receiver) returns error|int|boolean|byte|handle = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function errorDetail(handle receiver) returns error? = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;

public function uncheckedErrorDetail(handle receiver) returns int = @java:Method{
    class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"
} external;


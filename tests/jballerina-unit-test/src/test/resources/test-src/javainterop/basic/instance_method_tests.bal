import ballerina/java;

function testAcceptNothingAndReturnNothing(handle receiver) {
    increaseCounterByOne(receiver);
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

@java:Method{class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"}
public function increaseCounterByOne(handle receiver) = external;

@java:Method{class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods", name:"increaseCounterByOne"}
public function interopFunctionWithDifferentName(handle receiver) = external;

@java:Method{class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"}
public function getCounter(handle receiver) returns handle = external;

@java:Method{class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"}
public function setCounterValue(handle receiver, handle h) = external;

@java:Method{class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"}
public function setAndGetCounterValue(handle receiver, handle h) returns handle = external;

@java:Method{class:"org/ballerinalang/nativeimpl/jvm/tests/InstanceMethods"}
public function setTwiceAndGetCounterValue(handle receiver, handle h1, handle h2) returns handle = external;


import ballerina/java;

function testDefaultConstructor() returns handle {
    return newClassWithDefaultConstructor();
}

function testOneParamConstructor(handle h) returns handle {
    return newClassWithOneParamConstructor(h);
}

function testTwoParamConstructor(handle h1, handle h2) returns handle {
    return newClassWithTwoParamConstructor(h1, h2);
}

// Interop functions

@java:Constructor {class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithDefaultConstructor"}
public function newClassWithDefaultConstructor() returns handle = external;

@java:Constructor {class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithOneParamConstructor"}
public function newClassWithOneParamConstructor(handle h) returns handle = external;

@java:Constructor {class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithTwoParamConstructor"}
public function newClassWithTwoParamConstructor(handle h1, handle h2) returns handle = external;


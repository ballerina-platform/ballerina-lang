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

public function newClassWithDefaultConstructor() returns handle = @java:Constructor {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithDefaultConstructor"
} external;

public function newClassWithOneParamConstructor(handle h) returns handle = @java:Constructor {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithOneParamConstructor"
} external;

public function newClassWithTwoParamConstructor(handle h1, handle h2) returns handle = @java:Constructor {
    'class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithTwoParamConstructor"
} external;


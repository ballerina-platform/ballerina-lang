import ballerina/jballerina.java;

public class ClassA {

    public function newClassWithDefaultConstructor() returns handle = @java:Constructor {
        'class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithDefaultConstructor"
    } external;

    public function newClassWithOneParamConstructor(handle h) returns handle = @java:Constructor {
        'class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithOneParamConstructor"
    } external;

    public function newClassWithTwoParamConstructor(handle h1, handle h2) returns handle = @java:Constructor {
        'class:"org/ballerinalang/nativeimpl/jvm/tests/ClassWithTwoParamConstructor"
    } external;
}

function testDefaultConstructor() returns handle {
    return newClassWithDefaultConstructor();
}

function testOneParamConstructor(handle h) returns handle {
    return newClassWithOneParamConstructor(h);
}

function testTwoParamConstructor(handle h1, handle h2) returns handle {
    return newClassWithTwoParamConstructor(h1, h2);
}

function testDefaultConstructorForClass() returns handle {
    ClassA classA = new;
    return classA.newClassWithDefaultConstructor();
}

function testOneParamConstructorForClass(handle h) returns handle {
    ClassA classA = new;
    return classA.newClassWithOneParamConstructor(h);
}

function testTwoParamConstructorForClass(handle h1, handle h2) returns handle {
    ClassA classA = new;
    return classA.newClassWithTwoParamConstructor(h1, h2);
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


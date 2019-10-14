import ballerinax/java;

function testAcceptNothingAndReturnNothing() {
    acceptNothingAndReturnNothing();
}

function testInteropFunctionWithDifferentName() {
    interopFunctionWithDifferentName();
}

function testAcceptNothingButReturnDate() returns handle {
    return acceptNothingButReturnDate();
}

function testAcceptSomethingAndReturnSomething(handle h) returns handle {
    return acceptSomethingAndReturnSomething(h);
}

function testAcceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle {
    return acceptTwoParamsAndReturnSomething(h1, h2);
}

function testAcceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle {
    return acceptThreeParamsAndReturnSomething(h1, h2, h3);
}


// Interop functions
public function acceptNothingAndReturnNothing() = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function interopFunctionWithDifferentName() = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
    name:"acceptNothingAndReturnNothing"
} external;

public function acceptNothingButReturnDate() returns handle = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingAndReturnSomething(handle h) returns handle = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsCheckedException() returns error? = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsMultipleCheckedException() returns error? = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsUncheckedException() = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException() returns error? = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsCheckedException() returns handle | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsMultipleCheckedException() returns handle | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException() returns handle | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptNothingReturnSomethingAndThrowsUncheckedException() returns handle = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(handle h1) returns handle | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsCheckedException(handle h1) returns handle | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(handle h1) returns handle | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public function acceptSomethingReturnSomethingAndThrowsUncheckedException(handle h1) returns handle = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

public type Person object {
    int age = 9;
    public function __init(int age) {
        self.age = age;
    }
};

public function getObjectOrError() returns Person|error = @java:Method {
    name: "returnObjectOrError",
    class: "org.ballerinalang.test.javainterop.basic.StaticMethodTest"
} external;

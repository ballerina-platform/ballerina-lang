import ballerina/runtime;
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

function testErrorOrTupleReturn() returns error|[string,string] {
   [string,string] ret = check getArrayValue();
   return ret;
}

function testFuncWithAsyncDefaultParamExpression() returns int {
    return funcWithAsyncDefaultParamExpression() + funcWithAsyncDefaultParamExpression(5) + funcWithAsyncDefaultParamExpression(50, 20);
}

function asyncRet() returns int {
    runtime:sleep(50);
    return 10;
}

function asyncRetWithVal(int a = 30) returns int {
    runtime:sleep(50);
    return a + 20;
}

function testUsingParamValues() returns int {
    return usingParamValues() + usingParamValues(5) + usingParamValues(50, 20);
}

function testDecimalParamAndReturn(decimal a1) returns decimal {
    return decimalParamAndReturn(a1);
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

function getArrayValue() returns [string, string] | error = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function funcWithAsyncDefaultParamExpression(int a1 = asyncRet(), int a2 = asyncRet()) returns int = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function usingParamValues(int a1 = asyncRet(), int a2 = asyncRetWithVal(a1)) returns int = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;

function decimalParamAndReturn(decimal a1) returns decimal = @java:Method {
    class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods"
} external;


import ballerina/java;

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


// Interop functionsdsfs

@java:Method {class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods", isStatic:true}
public function acceptNothingAndReturnNothing() = external;

@java:Method {class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
              name:"acceptNothingAndReturnNothing",
              isStatic:true
}
public function interopFunctionWithDifferentName() = external;

@java:Method {class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods", isStatic:true}
public function acceptNothingButReturnDate() returns handle = external;

@java:Method {class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods", isStatic:true}
public function acceptSomethingAndReturnSomething(handle h) returns handle = external;

@java:Method {class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods", isStatic:true}
public function acceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle = external;

@java:Method {class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods", isStatic:true}
public function acceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle = external;


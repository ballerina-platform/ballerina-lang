import ballerina/java;

// Interop functions
public function acceptNothingAndReturnNothing() = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

public function interopFunctionWithDifferentName() = @java:Method {
    'class:"org.wso2.test.StaticMethods",
    name:"acceptNothingAndReturnNothing"
} external;

public function acceptNothingButReturnDate() returns handle = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

public function acceptSomethingAndReturnSomething(handle h) returns handle = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

public function acceptTwoParamsAndReturnSomething(handle h1, handle h2) returns handle = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

public function acceptThreeParamsAndReturnSomething(handle h1, handle h2, handle h3) returns handle = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

public function getString() returns handle = @java:Method {
    'class:"org.wso2.test.StaticMethods"
} external;

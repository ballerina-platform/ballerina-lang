function testStringFunctionInvocation() returns string {
    string s = "ballerina".toUpperAscii();
    return s;
}

function testStringFunctionInvocationInArgument() returns string {
    return greet("ballerina".toUpperAscii());
}

function greet(string s) returns string {
    return "Hello " + s;
}

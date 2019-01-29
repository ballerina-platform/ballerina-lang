function testStringFunctionInvocation() returns string {
    string s = "ballerina".toUpper();
    return s;
}

function testStringFunctionInvocationInArgument() returns string {
    return greet("ballerina".toUpper());
}

function greet(string s) returns string {
    return "Hello " + s;
}

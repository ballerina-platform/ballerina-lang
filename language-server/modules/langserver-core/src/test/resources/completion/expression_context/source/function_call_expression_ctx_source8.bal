import ballerina/module1;

// This test case tests the variable of type function type descriptor
function testFunction(function (json) returns boolean|error logic) {
    int valueA = 123;
    int valueB = 456;
    
}

# This is a test function with nil return type
#
# + a - Parameter a Description
# + b - Parameter b Description
function testFunctionWithParams1(int a, int b) returns () {
    return a+b;
}

# This is a test function without a return type
#
# + a - Parameter a Description
# + b - Parameter b Description
function testFunctionWithParams2(int a, int b) {
}

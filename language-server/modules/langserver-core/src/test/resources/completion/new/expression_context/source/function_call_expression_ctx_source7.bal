import ballerina/module1;

// This test case tests the documentation generation for the function completion item when the return type is nil
function testFunction() {
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

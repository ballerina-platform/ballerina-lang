import ballerina/module1;

function testFunction() {
    Record1 rec1 = new;
    rec1.testField.
}

# This is a test function
#
# + return - Return Value Description
function functionWithReturn() returns Record1 {
    return {};
}

type Record1 record {
    TestClass testField = new;
    int testOptional?;
};

class TestClass {
    int testClassField = 12;
    function testMethod() returns int {
        return 1;
    }
}
import ballerina/module1;

function testFunction() {
    TestClass cls = new;
    cls.testMethod().
}

# This is a test function
#
# + return - Return Value Description
function functionWithReturn() returns Record1 {
    return {};
}

type Record1 record {
    module1:TestRecord1 testField = {};
    int testOptional?;
};

class TestClass {
    function testMethod() returns Record1 {
        Record1 rec = {};
        
        return rec;
    }
}
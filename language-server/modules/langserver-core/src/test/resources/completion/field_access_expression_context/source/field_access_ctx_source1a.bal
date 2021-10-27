import ballerina/module1;

function testFunction() {
    module1:TestRecord1 rec = functionWithReturn().
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

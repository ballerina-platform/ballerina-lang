import ballerina/module1;

function testFunction() {
int value2= 12;
    var testVar = checkpanic module1:t
    int value1 = 12;
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

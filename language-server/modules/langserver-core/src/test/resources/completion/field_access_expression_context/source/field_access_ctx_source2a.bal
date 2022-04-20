import ballerina/module1;

function testFunction() {
    int t = functionWithReturn().t
}

# This is a test function
#
# + return - Return Value Description
function functionWithReturn() returns Record1 {
    return {
        intField: 100
    };
}

type Record1 record {
    module1:TestRecord1 testField = {};
    string strField1 = "";
    int testOptional?;
    int intField;
};

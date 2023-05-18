// import ballerina/module1;

function getFunction() returns module1:TestRecord2|error {
    return {rec2Field1: 10, rec3Field2: "rec3Field2"};
}

function testFunctionFunction() {
    getFunction();
}
